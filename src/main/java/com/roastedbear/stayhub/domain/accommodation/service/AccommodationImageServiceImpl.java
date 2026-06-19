package com.roastedbear.stayhub.domain.accommodation.service;

import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationImage;
import com.roastedbear.stayhub.domain.accommodation.repository.AccommodationImageRepository;
import com.roastedbear.stayhub.domain.accommodation.repository.AccommodationRepository;
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
 * 숙소 이미지 서비스 구현체
 *
 * 업로드 정책:
 *   - 최초 업로드 이미지가 자동으로 대표 이미지(isThumbnail=true) 지정
 *   - sortOrder: 기존 이미지 수를 기준으로 순차 부여
 *   - 한 번에 최대 10장 업로드 가능
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationImageServiceImpl implements AccommodationImageService {

    private static final String S3_FOLDER = "accommodations";
    private static final int MAX_UPLOAD_COUNT = 10;

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final S3Service s3Service;

    /**
     * 이미지 업로드
     * - 숙소에 이미지가 없을 때 첫 번째 업로드 이미지가 대표 이미지로 자동 지정
     * - 파일 유효성 검사: S3Service 위임 (이미지 형식, 10MB 제한)
     */
    @Override
    @Transactional
    public List<ImageUploadResponse> uploadImages(Long accommodationId, Long hostId,
                                                  List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
        if (files.size() > MAX_UPLOAD_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }

        Accommodation accommodation = findAndVerifyHost(accommodationId, hostId);

        // 기존 이미지 수 (sortOrder 시작점 결정)
        List<AccommodationImage> existing = accommodationImageRepository
                .findByAccommodationIdOrderBySortOrderAsc(accommodationId);
        int startOrder = existing.size();
        boolean noExistingImages = existing.isEmpty();

        List<ImageUploadResponse> responses = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String imageUrl = s3Service.upload(files.get(i), S3_FOLDER);

            // 숙소에 이미지가 없는 경우 첫 번째 파일이 대표 이미지
            boolean isThumbnail = noExistingImages && i == 0;

            AccommodationImage image = AccommodationImage.builder()
                    .accommodation(accommodation)
                    .imageUrl(imageUrl)
                    .isThumbnail(isThumbnail)
                    .sortOrder(startOrder + i)
                    .build();

            responses.add(ImageUploadResponse.from(
                    accommodationImageRepository.save(image)));
        }

        return responses;
    }

    /**
     * 이미지 삭제
     * - S3 파일 삭제 후 DB 레코드 삭제
     * - 삭제된 이미지가 대표 이미지였다면 다음 이미지를 대표 이미지로 자동 승격
     */
    @Override
    @Transactional
    public void deleteImage(Long accommodationId, Long imageId, Long hostId) {
        findAndVerifyHost(accommodationId, hostId);

        AccommodationImage image = findImageAndVerifyAccommodation(imageId, accommodationId);

        boolean wasThumbnail = image.isThumbnail();
        s3Service.delete(image.getImageUrl());
        accommodationImageRepository.delete(image);

        // 대표 이미지 삭제 시 다음 이미지를 대표 이미지로 승격
        if (wasThumbnail) {
            accommodationImageRepository
                    .findByAccommodationIdOrderBySortOrderAsc(accommodationId)
                    .stream()
                    .findFirst()
                    .ifPresent(AccommodationImage::setAsThumbnail);
        }
    }

    /**
     * 대표 이미지 변경
     * - 기존 대표 이미지 해제 후 새 대표 이미지 지정
     */
    @Override
    @Transactional
    public ImageUploadResponse setThumbnail(Long accommodationId, Long imageId, Long hostId) {
        findAndVerifyHost(accommodationId, hostId);

        // 기존 대표 이미지 해제
        accommodationImageRepository
                .findByAccommodationIdAndIsThumbnailTrue(accommodationId)
                .ifPresent(AccommodationImage::unsetThumbnail);

        // 새 대표 이미지 지정
        AccommodationImage image = findImageAndVerifyAccommodation(imageId, accommodationId);
        image.setAsThumbnail();

        return ImageUploadResponse.from(image);
    }

    // === Private 헬퍼 메서드 ===

    private Accommodation findAndVerifyHost(Long accommodationId, Long hostId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOMMODATION_NOT_FOUND));

        if (!accommodation.getHost().getId().equals(hostId)) {
            throw new BusinessException(ErrorCode.NOT_ACCOMMODATION_HOST);
        }

        return accommodation;
    }

    private AccommodationImage findImageAndVerifyAccommodation(Long imageId, Long accommodationId) {
        AccommodationImage image = accommodationImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getAccommodation().getId().equals(accommodationId)) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        return image;
    }
}
