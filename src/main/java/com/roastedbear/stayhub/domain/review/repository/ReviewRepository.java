package com.roastedbear.stayhub.domain.review.repository;

import com.roastedbear.stayhub.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 리뷰 레포지토리
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 숙소별 리뷰 목록 (최신순, 페이징)
    Page<Review> findByAccommodationIdOrderByCreatedAtDesc(Long accommodationId, Pageable pageable);

    // 예약에 대한 리뷰 존재 여부 (중복 작성 방지)
    boolean existsByReservationId(Long reservationId);

    // 예약으로 리뷰 조회
    Optional<Review> findByReservationId(Long reservationId);

    // 숙소 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.accommodation.id = :accommodationId")
    Optional<Double> findAverageRatingByAccommodationId(@Param("accommodationId") Long accommodationId);

    // 게스트가 작성한 리뷰 목록
    Page<Review> findByGuestIdOrderByCreatedAtDesc(Long guestId, Pageable pageable);

    // 숙소 리뷰 수
    long countByAccommodationId(Long accommodationId);
}
