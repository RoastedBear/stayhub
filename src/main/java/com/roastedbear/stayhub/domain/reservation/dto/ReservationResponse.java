package com.roastedbear.stayhub.domain.reservation.dto;

import com.roastedbear.stayhub.domain.reservation.entity.Reservation;
import com.roastedbear.stayhub.domain.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 예약 응답 DTO
 */
@Getter
@Builder
@Schema(description = "예약 응답")
public class ReservationResponse {

    @Schema(description = "예약 ID")
    private Long id;

    @Schema(description = "객실 ID")
    private Long roomId;

    @Schema(description = "객실명")
    private String roomName;

    @Schema(description = "숙소명")
    private String accommodationName;

    @Schema(description = "체크인 날짜")
    private LocalDate checkInDate;

    @Schema(description = "체크아웃 날짜")
    private LocalDate checkOutDate;

    @Schema(description = "투숙 인원")
    private Integer guestCount;

    @Schema(description = "총 결제 금액 (원)")
    private BigDecimal totalPrice;

    @Schema(description = "예약 상태 (PENDING/CONFIRMED/CANCELLED/COMPLETED)")
    private ReservationStatus status;

    @Schema(description = "예약 생성일시")
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .roomId(reservation.getRoom().getId())
                .roomName(reservation.getRoom().getName())
                .accommodationName(reservation.getRoom().getAccommodation().getName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestCount(reservation.getGuestCount())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
