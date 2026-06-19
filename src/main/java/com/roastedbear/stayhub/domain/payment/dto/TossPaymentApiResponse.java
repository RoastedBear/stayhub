package com.roastedbear.stayhub.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Toss Payments API 응답 역직렬화 DTO
 * - 결제 승인/취소 API 공통 응답 구조
 * - 알 수 없는 필드는 무시 (ignoreUnknown = true)
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentApiResponse {

    /** Toss가 발급하는 결제 고유 키 */
    private String paymentKey;

    /** 우리 서버에서 생성한 주문 ID */
    private String orderId;

    /** 결제 수단 (한글: "카드", "가상계좌", "간편결제", "계좌이체", "휴대폰") */
    private String method;

    /** 총 결제 금액 (원 단위) */
    private Long totalAmount;

    /** 결제 상태 (DONE, CANCELLED 등) */
    private String status;

    /** 결제 승인 시각 (ISO 8601: "2024-01-01T00:00:01+09:00") */
    private String approvedAt;
}
