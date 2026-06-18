package com.roastedbear.stayhub.domain.room.dto;

import com.roastedbear.stayhub.domain.room.entity.Room;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 객실 검색 결과 응답 DTO
 * - 목록 검색에 최적화된 경량 DTO (amenities 제외)
 */
@Getter
@Builder
@Schema(description = "객실 검색 결과")
public class RoomSearchResponse {

    @Schema(description = "객실 ID")
    private Long id;

    @Schema(description = "숙소 ID")
    private Long accommodationId;

    @Schema(description = "숙소명")
    private String accommodationName;

    @Schema(description = "시/도")
    private String sido;

    @Schema(description = "시/군/구")
    private String sigungu;

    @Schema(description = "상세 주소")
    private String address;

    @Schema(description = "객실명")
    private String name;

    @Schema(description = "객실 설명")
    private String description;

    @Schema(description = "최대 수용 인원")
    private Integer maxOccupancy;

    @Schema(description = "1박 기본 가격 (원)")
    private BigDecimal basePrice;

    public static RoomSearchResponse from(Room room) {
        return RoomSearchResponse.builder()
                .id(room.getId())
                .accommodationId(room.getAccommodation().getId())
                .accommodationName(room.getAccommodation().getName())
                .sido(room.getAccommodation().getSido())
                .sigungu(room.getAccommodation().getSigungu())
                .address(room.getAccommodation().getAddress())
                .name(room.getName())
                .description(room.getDescription())
                .maxOccupancy(room.getMaxOccupancy())
                .basePrice(room.getBasePrice())
                .build();
    }
}
