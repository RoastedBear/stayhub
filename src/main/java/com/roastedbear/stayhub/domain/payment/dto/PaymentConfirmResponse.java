package com.roastedbear.stayhub.domain.payment.dto;

import com.roastedbear.stayhub.domain.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 승인 응답 DTO
 */
@Getter
@Builder
public class PaymentConfirmResponse {

    private Long paymentId;
    private Long reservationId;
    private String orderId;
    private String paymentKey;
    private Long amount;
    private String method;
    private String status;
    private LocalDateTime approvedAt;

    public static PaymentConfirmResponse from(Payment payment) {
        return PaymentConfirmResponse.builder()
                .paymentId(payment.getId())
                .reservationId(payment.getReservation().getId())
                .orderId(payment.getOrderId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getAmount().longValue())
                .method(payment.getMethod() != null ? payment.getMethod().name() : null)
                .status(payment.getStatus().name())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}
