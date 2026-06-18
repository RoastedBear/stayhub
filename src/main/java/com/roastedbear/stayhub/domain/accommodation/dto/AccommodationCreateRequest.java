package com.roastedbear.stayhub.domain.accommodation.dto;

import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 숙소 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "숙소 등록 요청")
public class AccommodationCreateRequest {

    @NotBlank(message = "숙소명은 필수입니다.")
    @Size(max = 100, message = "숙소명은 100자를 초과할 수 없습니다.")
    @Schema(description = "숙소명", example = "제주 오션뷰 펜션")
    private String name;

    @Schema(description = "숙소 소개", example = "아름다운 오션뷰를 즐길 수 있는 펜션입니다.")
    private String description;

    @NotNull(message = "숙소 유형은 필수입니다.")
    @Schema(description = "숙소 유형 (HOTEL/PENSION/MOTEL/RESORT/GUESTHOUSE/VILLA)", example = "PENSION")
    private AccommodationType type;

    @NotBlank(message = "시/도는 필수입니다.")
    @Schema(description = "시/도", example = "제주특별자치도")
    private String sido;

    @NotBlank(message = "시/군/구는 필수입니다.")
    @Schema(description = "시/군/구", example = "서귀포시")
    private String sigungu;

    @NotBlank(message = "상세 주소는 필수입니다.")
    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다.")
    @Schema(description = "상세 주소", example = "서귀포시 중문관광로 000")
    private String address;

    @Schema(description = "위도", example = "33.2541")
    private Double latitude;

    @Schema(description = "경도", example = "126.5600")
    private Double longitude;
}
