package com.roastedbear.stayhub.domain.accommodation.dto;

import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationStatus;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 숙소 목록 응답 DTO (요약)
 * - 목록 조회 시 필요한 최소 정보만 포함
 */
@Getter
@Builder
@Schema(description = "숙소 목록 응답 (요약)")
public class AccommodationSummaryResponse {

    @Schema(description = "숙소 ID")
    private Long id;

    @Schema(description = "숙소명")
    private String name;

    @Schema(description = "숙소 유형")
    private AccommodationType type;

    @Schema(description = "시/도")
    private String sido;

    @Schema(description = "시/군/구")
    private String sigungu;

    @Schema(description = "상세 주소")
    private String address;

    @Schema(description = "숙소 상태")
    private AccommodationStatus status;

    public static AccommodationSummaryResponse from(Accommodation accommodation) {
        return AccommodationSummaryResponse.builder()
                .id(accommodation.getId())
                .name(accommodation.getName())
                .type(accommodation.getType())
                .sido(accommodation.getSido())
                .sigungu(accommodation.getSigungu())
                .address(accommodation.getAddress())
                .status(accommodation.getStatus())
                .build();
    }
}
