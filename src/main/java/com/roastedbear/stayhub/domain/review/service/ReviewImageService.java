package com.roastedbear.stayhub.domain.review.service;

import com.roastedbear.stayhub.global.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 리뷰 이미지 서비스 인터페이스
 */
public interface ReviewImageService {

    /** 리뷰에 이미지 추가 업로드 (리뷰 작성자 전용) */
    List<ImageUploadResponse> uploadImages(Long reviewId, Long memberId, List<MultipartFile> files);

    /** 리뷰 이미지 삭제 (리뷰 작성자 전용) */
    void deleteImage(Long reviewId, Long imageId, Long memberId);
}
