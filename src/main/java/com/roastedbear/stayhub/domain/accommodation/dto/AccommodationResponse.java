package com.roastedbear.stayhub.domain.accommodation.dto;

import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationStatus;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 숙소 상세 응답 DTO
 */
@Getter
@Builder
@Schema(description = "숙소 상세 응답")
public class AccommodationResponse {

    @Schema(description = "숙소 ID")
    private Long id;

    @Schema(description = "호스트 ID")
    private Long hostId;

    @Schema(description = "호스트 이름")
    private String hostName;

    @Schema(description = "숙소명")
    private String name;

    @Schema(description = "숙소 소개")
    private String description;

    @Schema(description = "숙소 유형")
    private AccommodationType type;

    @Schema(description = "시/도")
    private String sido;

    @Schema(description = "시/군/구")
    private String sigungu;

    @Schema(description = "상세 주소")
    private String address;

    @Schema(description = "위도")
    private Double latitude;

    @Schema(description = "경도")
    private Double longitude;

    @Schema(description = "숙소 상태")
    private AccommodationStatus status;

    @Schema(description = "등록일시")
    private LocalDateTime createdAt;

    public static AccommodationResponse from(Accommodation accommodation) {
        return AccommodationResponse.builder()
                .id(accommodation.getId())
                .hostId(accommodation.getHost().getId())
                .hostName(accommodation.getHost().getName())
                .name(accommodation.getName())
                .description(accommodation.getDescription())
                .type(accommodation.getType())
                .sido(accommodation.getSido())
                .sigungu(accommodation.getSigungu())
                .address(accommodation.getAddress())
                .latitude(accommodation.getLatitude())
                .longitude(accommodation.getLongitude())
                .status(accommodation.getStatus())
                .createdAt(accommodation.getCreatedAt())
                .build();
    }
}
