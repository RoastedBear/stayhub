package com.roastedbear.stayhub.domain.payment.service;

import com.roastedbear.stayhub.domain.payment.dto.PaymentCancelRequest;
import com.roastedbear.stayhub.domain.payment.dto.PaymentConfirmResponse;
import com.roastedbear.stayhub.domain.payment.dto.PaymentPrepareResponse;
import com.roastedbear.stayhub.domain.payment.dto.PaymentResponse;
import com.roastedbear.stayhub.domain.payment.dto.TossConfirmRequest;

/**
 * 결제 서비스 인터페이스
 */
public interface PaymentService {

    /**
     * 결제 준비
     * - Payment(PENDING) 레코드 생성 또는 기존 PENDING 결제 반환 (멱등성)
     * - 프론트에서 Toss 결제창 초기화에 사용할 orderId, amount, orderName 반환
     */
    PaymentPrepareResponse preparePayment(Long reservationId, Long memberId);

    /**
     * 결제 승인
     * - Toss 결제창 완료 후 프론트로부터 paymentKey, orderId, amount 수신
     * - Toss /v1/payments/confirm API 호출
     * - Payment 상태 DONE, Reservation 상태 CONFIRMED 변경
     */
    PaymentConfirmResponse confirmPayment(TossConfirmRequest request, Long memberId);

    /**
     * 결제 취소 (환불)
     * - Toss /v1/payments/{paymentKey}/cancel API 호출
     * - Payment 상태 CANCELLED, Reservation 상태 CANCELLED 변경
     */
    void cancelPayment(Long reservationId, PaymentCancelRequest request, Long memberId);

    /**
     * 결제 조회
     */
    PaymentResponse getPayment(Long reservationId, Long memberId);
}
