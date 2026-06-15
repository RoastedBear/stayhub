package com.roastedbear.stayhub.domain.payment.repository;

import com.roastedbear.stayhub.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 결제 레포지토리
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 예약으로 결제 조회 (1:1 관계)
    Optional<Payment> findByReservationId(Long reservationId);

    // Toss orderId로 결제 조회 (Toss 콜백 처리)
    Optional<Payment> findByOrderId(String orderId);

    // Toss paymentKey로 결제 조회 (취소 처리)
    Optional<Payment> findByPaymentKey(String paymentKey);
}
