package com.roastedbear.stayhub.domain.room.service;

import com.roastedbear.stayhub.domain.room.entity.Room;
import com.roastedbear.stayhub.domain.room.entity.RoomImage;
import com.roastedbear.stayhub.domain.room.repository.RoomImageRepository;
import com.roastedbear.stayhub.domain.room.repository.RoomRepository;
import com.roastedbear.stayhub.global.dto.ImageUploadResponse;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import com.roastedbear.stayhub.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 객실 이미지 서비스 구현체
 *
 * 호스트 검증: room → accommodation → host.id 비교
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomImageServiceImpl implements RoomImageService {

    private static final String S3_FOLDER = "rooms";
    private static final int MAX_UPLOAD_COUNT = 10;

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public List<ImageUploadResponse> uploadImages(Long roomId, Long hostId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
        if (files.size() > MAX_UPLOAD_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }

        Room room = findAndVerifyHost(roomId, hostId);

        List<RoomImage> existing = roomImageRepository.findByRoomIdOrderBySortOrderAsc(roomId);
        int startOrder = existing.size();
        boolean noExistingImages = existing.isEmpty();

        List<ImageUploadResponse> responses = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String imageUrl = s3Service.upload(files.get(i), S3_FOLDER);

            boolean isThumbnail = noExistingImages && i == 0;

            RoomImage image = RoomImage.builder()
                    .room(room)
                    .imageUrl(imageUrl)
                    .isThumbnail(isThumbnail)
                    .sortOrder(startOrder + i)
                    .build();

            responses.add(ImageUploadResponse.from(roomImageRepository.save(image)));
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteImage(Long roomId, Long imageId, Long hostId) {
        findAndVerifyHost(roomId, hostId);

        RoomImage image = findImageAndVerifyRoom(imageId, roomId);

        boolean wasThumbnail = image.isThumbnail();
        s3Service.delete(image.getImageUrl());
        roomImageRepository.delete(image);

        // 대표 이미지 삭제 시 다음 이미지 자동 승격
        if (wasThumbnail) {
            roomImageRepository.findByRoomIdOrderBySortOrderAsc(roomId)
                    .stream()
                    .findFirst()
                    .ifPresent(RoomImage::setAsThumbnail);
        }
    }

    @Override
    @Transactional
    public ImageUploadResponse setThumbnail(Long roomId, Long imageId, Long hostId) {
        findAndVerifyHost(roomId, hostId);

        roomImageRepository.findByRoomIdAndIsThumbnailTrue(roomId)
                .ifPresent(RoomImage::unsetThumbnail);

        RoomImage image = findImageAndVerifyRoom(imageId, roomId);
        image.setAsThumbnail();

        return ImageUploadResponse.from(image);
    }

    // === Private 헬퍼 메서드 ===

    private Room findAndVerifyHost(Long roomId, Long hostId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getAccommodation().getHost().getId().equals(hostId)) {
            throw new BusinessException(ErrorCode.NOT_ACCOMMODATION_HOST);
        }

        return room;
    }

    private RoomImage findImageAndVerifyRoom(Long imageId, Long roomId) {
        RoomImage image = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getRoom().getId().equals(roomId)) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        return image;
    }
}
