package com.roastedbear.stayhub.domain.reservation.entity;

/**
 * 예약 상태
 */
public enum ReservationStatus {
    PENDING,    // 결제 대기 (예약 요청됨)
    CONFIRMED,  // 예약 확정 (결제 완료)
    CANCELLED,  // 취소됨
    COMPLETED   // 이용 완료 (체크아웃 후)
}
