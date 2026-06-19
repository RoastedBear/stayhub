package com.roastedbear.stayhub.domain.review.service;

import com.roastedbear.stayhub.domain.review.entity.Review;
import com.roastedbear.stayhub.domain.review.entity.ReviewImage;
import com.roastedbear.stayhub.domain.review.repository.ReviewImageRepository;
import com.roastedbear.stayhub.domain.review.repository.ReviewRepository;
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
 * 리뷰 이미지 서비스 구현체
 *
 * - 리뷰 작성자 본인만 이미지 추가/삭제 가능
 * - sortOrder: 기존 이미지 수를 기준으로 순차 부여
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewImageServiceImpl implements ReviewImageService {

    private static final String S3_FOLDER = "reviews";
    private static final int MAX_UPLOAD_COUNT = 10;

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public List<ImageUploadResponse> uploadImages(Long reviewId, Long memberId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
        if (files.size() > MAX_UPLOAD_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }

        Review review = findReviewAndCheckOwner(reviewId, memberId);

        // 기존 이미지 수 기반으로 sortOrder 결정
        List<ReviewImage> existing = reviewImageRepository.findByReviewIdOrderBySortOrderAsc(reviewId);
        int startOrder = existing.size();

        List<ImageUploadResponse> responses = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String imageUrl = s3Service.upload(files.get(i), S3_FOLDER);

            ReviewImage image = ReviewImage.builder()
                    .review(review)
                    .imageUrl(imageUrl)
                    .sortOrder(startOrder + i)
                    .build();

            responses.add(ImageUploadResponse.from(reviewImageRepository.save(image)));
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteImage(Long reviewId, Long imageId, Long memberId) {
        findReviewAndCheckOwner(reviewId, memberId);

        ReviewImage image = reviewImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        // 해당 리뷰의 이미지인지 확인
        if (!image.getReview().getId().equals(reviewId)) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        s3Service.delete(image.getImageUrl());
        reviewImageRepository.delete(image);
    }

    // === Private 헬퍼 메서드 ===

    private Review findReviewAndCheckOwner(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getGuest().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        return review;
    }
}
