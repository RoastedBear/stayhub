package com.roastedbear.stayhub.global.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationImage;
import com.roastedbear.stayhub.domain.review.entity.ReviewImage;
import com.roastedbear.stayhub.domain.room.entity.RoomImage;
import lombok.Builder;
import lombok.Getter;

/**
 * 이미지 업로드 공통 응답 DTO
 * - 숙소/객실/리뷰 이미지 업로드 API 공통 사용
 */
@Getter
@Builder
public class ImageUploadResponse {

    private Long imageId;
    private String imageUrl;
    @JsonProperty("isThumbnail")
    private boolean isThumbnail;
    private Integer sortOrder;

    public static ImageUploadResponse from(AccommodationImage image) {
        return ImageUploadResponse.builder()
                .imageId(image.getId())
                .imageUrl(image.getImageUrl())
                .isThumbnail(image.isThumbnail())
                .sortOrder(image.getSortOrder())
                .build();
    }

    public static ImageUploadResponse from(RoomImage image) {
        return ImageUploadResponse.builder()
                .imageId(image.getId())
                .imageUrl(image.getImageUrl())
                .isThumbnail(image.isThumbnail())
                .sortOrder(image.getSortOrder())
                .build();
    }

    public static ImageUploadResponse from(ReviewImage image) {
        return ImageUploadResponse.builder()
                .imageId(image.getId())
                .imageUrl(image.getImageUrl())
                .isThumbnail(false)
                .sortOrder(image.getSortOrder())
                .build();
    }
}
