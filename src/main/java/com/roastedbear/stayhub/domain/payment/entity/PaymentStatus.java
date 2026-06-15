package com.roastedbear.stayhub.domain.payment.entity;

/**
 * Toss Payments 결제 상태 (Toss API 상태값과 동일하게 맞춤)
 */
public enum PaymentStatus {
    PENDING,           // 결제 대기
    DONE,              // 결제 완료
    CANCELLED,         // 전액 취소
    PARTIAL_CANCELLED, // 부분 취소
    FAILED             // 결제 실패
}
