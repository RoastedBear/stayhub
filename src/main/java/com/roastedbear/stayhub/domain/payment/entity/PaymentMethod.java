package com.roastedbear.stayhub.domain.payment.entity;

/**
 * Toss Payments 결제 수단
 */
public enum PaymentMethod {
    CARD,            // 카드
    VIRTUAL_ACCOUNT, // 가상계좌
    EASY_PAY,        // 간편결제 (카카오페이, 네이버페이 등)
    BANK_TRANSFER,   // 계좌이체
    MOBILE_PHONE     // 휴대폰 소액결제
}
