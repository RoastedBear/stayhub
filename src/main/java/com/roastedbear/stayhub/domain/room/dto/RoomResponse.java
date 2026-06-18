package com.roastedbear.stayhub.domain.room.dto;

import com.roastedbear.stayhub.domain.room.entity.Room;
import com.roastedbear.stayhub.domain.room.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 객실 상세 응답 DTO
 */
@Getter
@Builder
@Schema(description = "객실 상세 응답")
public class RoomResponse {

    @Schema(description = "객실 ID")
    private Long id;

    @Schema(description = "숙소 ID")
    private Long accommodationId;

    @Schema(description = "숙소명")
    private String accommodationName;

    @Schema(description = "객실명")
    private String name;

    @Schema(description = "객실 설명")
    private String description;

    @Schema(description = "최대 수용 인원")
    private Integer maxOccupancy;

    @Schema(description = "1박 기본 가격 (원)")
    private BigDecimal basePrice;

    @Schema(description = "객실 상태")
    private RoomStatus status;

    @Schema(description = "편의시설 목록")
    private List<AmenityResponse> amenities;

    public static RoomResponse from(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .accommodationId(room.getAccommodation().getId())
                .accommodationName(room.getAccommodation().getName())
                .name(room.getName())
                .description(room.getDescription())
                .maxOccupancy(room.getMaxOccupancy())
                .basePrice(room.getBasePrice())
                .status(room.getStatus())
                .amenities(room.getAmenities().stream()
                        .map(AmenityResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
