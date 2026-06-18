package com.roastedbear.stayhub.domain.accommodation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 숙소 수정 요청 DTO
 * - 유형(type)과 지역(sido/sigungu)은 수정 불가
 */
@Getter
@NoArgsConstructor
@Schema(description = "숙소 수정 요청")
public class AccommodationUpdateRequest {

    @NotBlank(message = "숙소명은 필수입니다.")
    @Size(max = 100, message = "숙소명은 100자를 초과할 수 없습니다.")
    @Schema(description = "숙소명", example = "제주 오션뷰 펜션 리뉴얼")
    private String name;

    @Schema(description = "숙소 소개")
    private String description;

    @NotBlank(message = "상세 주소는 필수입니다.")
    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다.")
    @Schema(description = "상세 주소")
    private String address;

    @Schema(description = "위도")
    private Double latitude;

    @Schema(description = "경도")
    private Double longitude;
}
