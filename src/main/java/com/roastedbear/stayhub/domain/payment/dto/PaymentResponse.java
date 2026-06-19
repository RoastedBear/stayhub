package com.roastedbear.stayhub.domain.payment.dto;

import com.roastedbear.stayhub.domain.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 조회 응답 DTO
 */
@Getter
@Builder
public class PaymentResponse {

    private Long paymentId;
    private Long reservationId;
    private String orderId;
    private String paymentKey;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .reservationId(payment.getReservation().getId())
                .orderId(payment.getOrderId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getAmount())
                .method(payment.getMethod() != null ? payment.getMethod().name() : null)
                .status(payment.getStatus().name())
                .approvedAt(payment.getApprovedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
