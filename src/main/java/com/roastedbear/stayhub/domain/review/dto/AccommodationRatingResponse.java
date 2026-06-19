package com.roastedbear.stayhub.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 숙소 평균 평점 응답 DTO
 */
@Getter
@Builder
public class AccommodationRatingResponse {

    private Long accommodationId;
    /** 평균 평점 (소수 첫째 자리 반올림, 리뷰 없으면 null) */
    private Double averageRating;
    private Long reviewCount;
}
