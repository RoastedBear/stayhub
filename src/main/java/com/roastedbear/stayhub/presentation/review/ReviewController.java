package com.roastedbear.stayhub.presentation.review;

import com.roastedbear.stayhub.domain.review.dto.AccommodationRatingResponse;
import com.roastedbear.stayhub.domain.review.dto.ReviewCreateRequest;
import com.roastedbear.stayhub.domain.review.dto.ReviewResponse;
import com.roastedbear.stayhub.domain.review.dto.ReviewUpdateRequest;
import com.roastedbear.stayhub.domain.review.service.ReviewService;
import com.roastedbear.stayhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 리뷰 API 컨트롤러
 *
 * 인증 필요 (JWT):
 *   POST   /api/reviews                                   - 리뷰 작성
 *   PUT    /api/reviews/{reviewId}                        - 리뷰 수정
 *   DELETE /api/reviews/{reviewId}                        - 리뷰 삭제
 *
 * 공개 (인증 불필요):
 *   GET    /api/reviews/accommodations/{accommodationId}         - 숙소별 리뷰 목록
 *   GET    /api/reviews/accommodations/{accommodationId}/rating  - 숙소 평균 평점
 */
@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성
     * POST /api/reviews
     *
     * - COMPLETED(체크아웃 완료) 예약에 대해서만 작성 가능
     * - 예약당 1개 제한
     */
    @Operation(
            summary = "리뷰 작성",
            description = """
                    체크아웃 완료(COMPLETED) 예약에 대해 리뷰를 작성합니다.
                    - 예약당 1개의 리뷰만 작성할 수 있습니다.
                    - 이미지 URL은 최대 10장까지 첨부 가능합니다.
                    - 평점은 1~5점 사이의 정수입니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = reviewService.createReview(
                userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 리뷰 수정
     * PUT /api/reviews/{reviewId}
     *
     * - 본인이 작성한 리뷰만 수정 가능
     * - 이미지는 전체 교체 방식 (기존 이미지 삭제 후 새 이미지 저장)
     */
    @Operation(
            summary = "리뷰 수정",
            description = """
                    본인이 작성한 리뷰를 수정합니다.
                    - 이미지는 전체 교체 방식으로 업데이트됩니다. (기존 이미지 삭제 후 새로 저장)
                    - 이미지를 유지하려면 기존 이미지 URL을 그대로 포함해야 합니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewResponse response = reviewService.updateReview(
                userDetails.getMemberId(), reviewId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제
     * DELETE /api/reviews/{reviewId}
     *
     * - 본인이 작성한 리뷰만 삭제 가능
     */
    @Operation(
            summary = "리뷰 삭제",
            description = "본인이 작성한 리뷰를 삭제합니다."
    )
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId) {
        reviewService.deleteReview(userDetails.getMemberId(), reviewId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 숙소별 리뷰 목록 조회
     * GET /api/reviews/accommodations/{accommodationId}
     *
     * - 인증 불필요 (공개 API)
     * - 최신순 정렬, 기본 10건 페이징
     */
    @Operation(
            summary = "숙소별 리뷰 목록 조회",
            description = """
                    숙소의 리뷰 목록을 최신순으로 조회합니다. (로그인 불필요)
                    - 기본 10건씩 페이징합니다.
                    - 각 리뷰에 이미지 목록이 포함됩니다.
                    """
    )
    @GetMapping("/accommodations/{accommodationId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByAccommodation(
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                reviewService.getReviewsByAccommodation(accommodationId, pageable));
    }

    /**
     * 숙소 평균 평점 조회
     * GET /api/reviews/accommodations/{accommodationId}/rating
     *
     * - 인증 불필요 (공개 API)
     * - 리뷰가 없으면 averageRating: null 반환
     */
    @Operation(
            summary = "숙소 평균 평점 조회",
            description = """
                    숙소의 평균 평점과 리뷰 수를 조회합니다. (로그인 불필요)
                    - 평균 평점은 소수 첫째 자리에서 반올림합니다.
                    - 리뷰가 없으면 averageRating은 null을 반환합니다.
                    """
    )
    @GetMapping("/accommodations/{accommodationId}/rating")
    public ResponseEntity<AccommodationRatingResponse> getAverageRating(
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId) {
        return ResponseEntity.ok(reviewService.getAverageRating(accommodationId));
    }
}
