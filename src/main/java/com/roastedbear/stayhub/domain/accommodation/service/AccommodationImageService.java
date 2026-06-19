package com.roastedbear.stayhub.domain.accommodation.service;

import com.roastedbear.stayhub.global.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 숙소 이미지 서비스 인터페이스
 */
public interface AccommodationImageService {

    /** 이미지 업로드 (호스트 전용) - 최초 업로드된 이미지가 자동으로 대표 이미지 지정 */
    List<ImageUploadResponse> uploadImages(Long accommodationId, Long hostId,
                                           List<MultipartFile> files);

    /** 이미지 삭제 (호스트 전용) - S3 + DB 동시 삭제 */
    void deleteImage(Long accommodationId, Long imageId, Long hostId);

    /** 대표 이미지 변경 (호스트 전용) */
    ImageUploadResponse setThumbnail(Long accommodationId, Long imageId, Long hostId);
}
