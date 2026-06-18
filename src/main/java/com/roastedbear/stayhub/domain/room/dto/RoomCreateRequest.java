package com.roastedbear.stayhub.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 객실 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "객실 등록 요청")
public class RoomCreateRequest {

    @NotBlank(message = "객실명은 필수입니다.")
    @Size(max = 100, message = "객실명은 100자를 초과할 수 없습니다.")
    @Schema(description = "객실명", example = "스탠다드 더블")
    private String name;

    @Schema(description = "객실 설명", example = "넓고 쾌적한 더블 객실입니다.")
    private String description;

    @NotNull(message = "최대 수용 인원은 필수입니다.")
    @Min(value = 1, message = "최대 수용 인원은 1명 이상이어야 합니다.")
    @Schema(description = "최대 수용 인원", example = "2")
    private Integer maxOccupancy;

    @NotNull(message = "기본 가격은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
    @Schema(description = "1박 기본 가격 (원)", example = "100000")
    private BigDecimal basePrice;

    @Schema(description = "편의시설 ID 목록", example = "[1, 2, 3]")
    private List<Long> amenityIds = new ArrayList<>();
}
