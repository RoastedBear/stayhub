package com.roastedbear.stayhub.domain.room.dto;

import com.roastedbear.stayhub.domain.room.entity.Amenity;
import com.roastedbear.stayhub.domain.room.entity.AmenityCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 편의시설 응답 DTO
 */
@Getter
@Builder
@Schema(description = "편의시설 정보")
public class AmenityResponse {

    @Schema(description = "편의시설 ID")
    private Long id;

    @Schema(description = "편의시설명", example = "에어컨")
    private String name;

    @Schema(description = "카테고리", example = "BASIC")
    private AmenityCategory category;

    public static AmenityResponse from(Amenity amenity) {
        return AmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .category(amenity.getCategory())
                .build();
    }
}
