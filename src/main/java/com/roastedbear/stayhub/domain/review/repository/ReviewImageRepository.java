package com.roastedbear.stayhub.domain.review.repository;

import com.roastedbear.stayhub.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 리뷰 이미지 레포지토리
 */
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    // 리뷰 이미지 목록 (정렬 순서대로)
    List<ReviewImage> findByReviewIdOrderBySortOrderAsc(Long reviewId);

    // 리뷰 이미지 전체 삭제 (리뷰 삭제 시)
    void deleteByReviewId(Long reviewId);

    // 여러 리뷰의 이미지를 한 번에 조회 (N+1 방지)
    List<ReviewImage> findByReviewIdInOrderBySortOrderAsc(List<Long> reviewIds);
}
