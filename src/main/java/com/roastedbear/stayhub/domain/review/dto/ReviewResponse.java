package com.roastedbear.stayhub.domain.review.dto;

import com.roastedbear.stayhub.domain.review.entity.Review;
import com.roastedbear.stayhub.domain.review.entity.ReviewImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 리뷰 응답 DTO
 */
@Getter
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long reservationId;
    private Long accommodationId;
    private Long guestId;
    /** 작성자 이름 (공개용) */
    private String guestName;
    private Integer rating;
    private String content;
    private List<ReviewImageDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse from(Review review, List<ReviewImage> images) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .reservationId(review.getReservation().getId())
                .accommodationId(review.getAccommodation().getId())
                .guestId(review.getGuest().getId())
                .guestName(review.getGuest().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .images(images.stream()
                        .map(ReviewImageDto::from)
                        .collect(Collectors.toList()))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
