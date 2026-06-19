package com.roastedbear.stayhub.domain.review.service;

import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.domain.accommodation.repository.AccommodationRepository;
import com.roastedbear.stayhub.domain.reservation.entity.Reservation;
import com.roastedbear.stayhub.domain.reservation.entity.ReservationStatus;
import com.roastedbear.stayhub.domain.reservation.repository.ReservationRepository;
import com.roastedbear.stayhub.domain.review.dto.AccommodationRatingResponse;
import com.roastedbear.stayhub.domain.review.dto.ReviewCreateRequest;
import com.roastedbear.stayhub.domain.review.dto.ReviewResponse;
import com.roastedbear.stayhub.domain.review.dto.ReviewUpdateRequest;
import com.roastedbear.stayhub.domain.review.entity.Review;
import com.roastedbear.stayhub.domain.review.entity.ReviewImage;
import com.roastedbear.stayhub.domain.review.repository.ReviewImageRepository;
import com.roastedbear.stayhub.domain.review.repository.ReviewRepository;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 리뷰 서비스 구현체
 *
 * 작성 제약:
 *   - 예약 상태 COMPLETED (체크아웃 완료) 확인
 *   - 예약당 리뷰 1건 (REVIEW_ALREADY_EXISTS)
 *   - 본인 예약이 아닌 경우 404 처리 (정보 노출 방지)
 *
 * 이미지 처리:
 *   - URL 직접 입력 방식 (S3 연동 전)
 *   - 수정 시 전체 교체 방식 (기존 삭제 → 새로 저장)
 *
 * N+1 방지:
 *   - 목록 조회 시 reviewId IN (...) 한 번의 쿼리로 이미지 일괄 로딩
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReservationRepository reservationRepository;
    private final AccommodationRepository accommodationRepository;

    /**
     * 리뷰 작성
     * 1. 예약 조회 + 본인 예약 검증
     * 2. COMPLETED 상태 검증
     * 3. 중복 작성 검증
     * 4. Review + ReviewImage 저장
     */
    @Override
    @Transactional
    public ReviewResponse createReview(Long memberId, ReviewCreateRequest request) {
        Reservation reservation = findReservationAndCheckOwner(request.getReservationId(), memberId);

        // 체크아웃 완료 예약에만 리뷰 작성 가능
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_COMPLETED);
        }

        // 예약당 리뷰 1건 제한
        if (reviewRepository.existsByReservationId(request.getReservationId())) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 숙소 정보 (예약 → 객실 → 숙소 체인 이용)
        Accommodation accommodation = reservation.getRoom().getAccommodation();

        Review review = Review.builder()
                .reservation(reservation)
                .guest(reservation.getGuest())
                .accommodation(accommodation)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewRepository.save(review);

        // 이미지 저장 (URL 순서대로 sortOrder 부여)
        List<ReviewImage> savedImages = saveImages(review, request.getImageUrls());

        return ReviewResponse.from(review, savedImages);
    }

    /**
     * 리뷰 수정
     * - 이미지 교체: 기존 이미지 전체 삭제 → 새 이미지 저장
     */
    @Override
    @Transactional
    public ReviewResponse updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request) {
        Review review = findReviewAndCheckOwner(reviewId, memberId);

        review.update(request.getRating(), request.getContent());

        // 기존 이미지 삭제 후 새 이미지 저장
        reviewImageRepository.deleteByReviewId(reviewId);
        List<ReviewImage> savedImages = saveImages(review, request.getImageUrls());

        return ReviewResponse.from(review, savedImages);
    }

    /**
     * 리뷰 삭제
     * - 이미지 FK 제약으로 인해 이미지 먼저 삭제 후 리뷰 삭제
     */
    @Override
    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        Review review = findReviewAndCheckOwner(reviewId, memberId);

        reviewImageRepository.deleteByReviewId(reviewId);
        reviewRepository.delete(review);
    }

    /**
     * 숙소별 리뷰 목록 조회
     * - 이미지 N+1 방지: 페이지의 reviewId 목록으로 이미지 일괄 조회 후 그루핑
     */
    @Override
    public Page<ReviewResponse> getReviewsByAccommodation(Long accommodationId, Pageable pageable) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new BusinessException(ErrorCode.ACCOMMODATION_NOT_FOUND);
        }

        Page<Review> reviewPage = reviewRepository
                .findByAccommodationIdOrderByCreatedAtDesc(accommodationId, pageable);

        // 이미지 일괄 조회 (N+1 방지)
        List<Long> reviewIds = reviewPage.getContent().stream()
                .map(Review::getId)
                .collect(Collectors.toList());

        Map<Long, List<ReviewImage>> imagesByReviewId = Collections.emptyMap();
        if (!reviewIds.isEmpty()) {
            imagesByReviewId = reviewImageRepository
                    .findByReviewIdInOrderBySortOrderAsc(reviewIds)
                    .stream()
                    .collect(Collectors.groupingBy(img -> img.getReview().getId()));
        }

        final Map<Long, List<ReviewImage>> imageMap = imagesByReviewId;
        return reviewPage.map(review ->
                ReviewResponse.from(
                        review,
                        imageMap.getOrDefault(review.getId(), Collections.emptyList())
                )
        );
    }

    /**
     * 숙소 평균 평점 조회
     * - 소수 첫째 자리 반올림 (4.35 → 4.4)
     */
    @Override
    public AccommodationRatingResponse getAverageRating(Long accommodationId) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new BusinessException(ErrorCode.ACCOMMODATION_NOT_FOUND);
        }

        Double rawAverage = reviewRepository
                .findAverageRatingByAccommodationId(accommodationId)
                .orElse(null);

        // 소수 첫째 자리 반올림
        Double averageRating = rawAverage != null
                ? BigDecimal.valueOf(rawAverage).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : null;

        long reviewCount = reviewRepository.countByAccommodationId(accommodationId);

        return AccommodationRatingResponse.builder()
                .accommodationId(accommodationId)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .build();
    }

    // === Private 헬퍼 메서드 ===

    /** 예약 조회 + 본인 소유 검증 (타인 예약 접근 시 404) */
    private Reservation findReservationAndCheckOwner(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getGuest().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservation;
    }

    /** 리뷰 조회 + 본인 소유 검증 (타인 리뷰 접근 시 404) */
    private Review findReviewAndCheckOwner(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getGuest().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }

        return review;
    }

    /** URL 목록을 ReviewImage 엔티티로 변환하여 저장 */
    private List<ReviewImage> saveImages(Review review, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReviewImage> images = IntStream.range(0, imageUrls.size())
                .mapToObj(i -> ReviewImage.builder()
                        .review(review)
                        .imageUrl(imageUrls.get(i))
                        .sortOrder(i)
                        .build())
                .collect(Collectors.toList());

        return reviewImageRepository.saveAll(images);
    }
}
