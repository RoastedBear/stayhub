package com.roastedbear.stayhub.domain.review.service;

import com.roastedbear.stayhub.domain.review.dto.AccommodationRatingResponse;
import com.roastedbear.stayhub.domain.review.dto.ReviewCreateRequest;
import com.roastedbear.stayhub.domain.review.dto.ReviewResponse;
import com.roastedbear.stayhub.domain.review.dto.ReviewUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 리뷰 서비스 인터페이스
 */
public interface ReviewService {

    /**
     * 리뷰 작성
     * - COMPLETED 상태 예약에 대해서만 작성 가능
     * - 예약당 1개 제한
     */
    ReviewResponse createReview(Long memberId, ReviewCreateRequest request);

    /**
     * 리뷰 수정 (본인만 가능)
     * - 이미지는 전체 교체 방식
     */
    ReviewResponse updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request);

    /**
     * 리뷰 삭제 (본인만 가능)
     */
    void deleteReview(Long memberId, Long reviewId);

    /**
     * 숙소별 리뷰 목록 조회 (최신순, 페이징)
     */
    Page<ReviewResponse> getReviewsByAccommodation(Long accommodationId, Pageable pageable);

    /**
     * 숙소 평균 평점 조회
     */
    AccommodationRatingResponse getAverageRating(Long accommodationId);
}
