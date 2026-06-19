package com.roastedbear.stayhub.domain.room.service;

import com.roastedbear.stayhub.global.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 객실 이미지 서비스 인터페이스
 */
public interface RoomImageService {

    /** 이미지 업로드 (호스트 전용) */
    List<ImageUploadResponse> uploadImages(Long roomId, Long hostId, List<MultipartFile> files);

    /** 이미지 삭제 (호스트 전용) */
    void deleteImage(Long roomId, Long imageId, Long hostId);

    /** 대표 이미지 변경 (호스트 전용) */
    ImageUploadResponse setThumbnail(Long roomId, Long imageId, Long hostId);
}
