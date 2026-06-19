package com.roastedbear.stayhub.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제 준비 응답 DTO
 * - 프론트에서 Toss 결제창 초기화 시 필요한 정보
 * - orderId: Toss 결제창에 넘길 주문 ID (UUID)
 * - amount: Toss 결제창에 넘길 금액 (원 단위 Long)
 * - orderName: 결제창에 표시될 주문명
 */
@Getter
@Builder
public class PaymentPrepareResponse {

    private Long paymentId;
    private Long reservationId;
    private String orderId;
    private String orderName;
    private Long amount;
}
