package com.roastedbear.stayhub.domain.review.dto;

import com.roastedbear.stayhub.domain.review.entity.ReviewImage;
import lombok.Builder;
import lombok.Getter;

/**
 * 리뷰 이미지 응답 DTO
 */
@Getter
@Builder
public class ReviewImageDto {

    private Long imageId;
    private String imageUrl;
    private Integer sortOrder;

    public static ReviewImageDto from(ReviewImage image) {
        return ReviewImageDto.builder()
                .imageId(image.getId())
                .imageUrl(image.getImageUrl())
                .sortOrder(image.getSortOrder())
                .build();
    }
}
