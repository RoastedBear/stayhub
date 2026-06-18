package com.roastedbear.stayhub.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 예약 생성 요청 DTO
 * - 날짜 순서(checkIn < checkOut) 검증은 서비스 계층에서 수행
 */
@Getter
@NoArgsConstructor
@Schema(description = "예약 생성 요청")
public class ReservationCreateRequest {

    @NotNull(message = "객실 ID는 필수입니다.")
    @Schema(description = "예약할 객실 ID", example = "1")
    private Long roomId;

    @NotNull(message = "체크인 날짜는 필수입니다.")
    @Schema(description = "체크인 날짜 (오늘 포함 이후)", example = "2026-07-01")
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜는 필수입니다.")
    @Schema(description = "체크아웃 날짜 (체크인 다음 날 이후)", example = "2026-07-03")
    private LocalDate checkOutDate;

    @NotNull(message = "투숙 인원은 필수입니다.")
    @Min(value = 1, message = "투숙 인원은 1명 이상이어야 합니다.")
    @Schema(description = "투숙 인원", example = "2")
    private Integer guestCount;
}
