package com.roastedbear.stayhub.domain.payment.service;

import com.roastedbear.stayhub.domain.payment.dto.PaymentCancelRequest;
import com.roastedbear.stayhub.domain.payment.dto.PaymentConfirmResponse;
import com.roastedbear.stayhub.domain.payment.dto.PaymentPrepareResponse;
import com.roastedbear.stayhub.domain.payment.dto.PaymentResponse;
import com.roastedbear.stayhub.domain.payment.dto.TossConfirmRequest;
import com.roastedbear.stayhub.domain.payment.dto.TossPaymentApiResponse;
import com.roastedbear.stayhub.domain.payment.entity.Payment;
import com.roastedbear.stayhub.domain.payment.entity.PaymentMethod;
import com.roastedbear.stayhub.domain.payment.entity.PaymentStatus;
import com.roastedbear.stayhub.domain.payment.repository.PaymentRepository;
import com.roastedbear.stayhub.domain.reservation.entity.Reservation;
import com.roastedbear.stayhub.domain.reservation.repository.ReservationRepository;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 결제 서비스 구현체
 *
 * 결제 플로우:
 *   1. preparePayment: Payment(PENDING) 생성 → orderId, amount 반환
 *   2. (프론트) Toss 결제창 호출 → 사용자 결제
 *   3. confirmPayment: Toss confirm API 호출 → Payment DONE, Reservation CONFIRMED
 *   4. cancelPayment: Toss cancel API 호출 → Payment CANCELLED, Reservation CANCELLED
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final String TOSS_CONFIRM_PATH = "/v1/payments/confirm";
    private static final String TOSS_CANCEL_PATH = "/v1/payments/{paymentKey}/cancel";

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final WebClient tossWebClient;

    /**
     * 결제 준비
     * - 이미 DONE 결제 → ALREADY_PAID 예외
     * - 기존 PENDING 결제 → 기존 orderId 재사용 (멱등성)
     * - 신규 → Payment(PENDING) 생성
     */
    @Override
    @Transactional
    public PaymentPrepareResponse preparePayment(Long reservationId, Long memberId) {
        Reservation reservation = findReservationAndCheckOwner(reservationId, memberId);

        // 기존 결제 레코드 확인 (예약당 결제 1건)
        paymentRepository.findByReservationId(reservationId).ifPresent(existing -> {
            if (existing.getStatus() == PaymentStatus.DONE) {
                throw new BusinessException(ErrorCode.ALREADY_PAID);
            }
        });

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseGet(() -> {
                    Payment newPayment = Payment.builder()
                            .reservation(reservation)
                            .member(reservation.getGuest())
                            .orderId(UUID.randomUUID().toString())
                            .amount(reservation.getTotalPrice())
                            .build();
                    return paymentRepository.save(newPayment);
                });

        return PaymentPrepareResponse.builder()
                .paymentId(payment.getId())
                .reservationId(reservationId)
                .orderId(payment.getOrderId())
                .orderName(buildOrderName(reservation))
                .amount(payment.getAmount().longValue())
                .build();
    }

    /**
     * 결제 승인
     * 1. orderId로 Payment 조회
     * 2. 본인 결제인지 검증
     * 3. 금액 일치 검증
     * 4. Toss confirm API 호출
     * 5. Payment.approve() + Reservation.confirm()
     */
    @Override
    @Transactional
    public PaymentConfirmResponse confirmPayment(TossConfirmRequest request, Long memberId) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 본인 결제 검증
        if (!payment.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        // 금액 위변조 방지: 서버 저장 금액과 Toss 콜백 금액 비교
        if (payment.getAmount().compareTo(BigDecimal.valueOf(request.getAmount())) != 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // Toss API 결제 승인 호출
        TossPaymentApiResponse tossResponse = callTossConfirmApi(request);

        // 결제 승인 처리
        payment.approve(
                tossResponse.getPaymentKey(),
                mapTossMethod(tossResponse.getMethod()),
                parseApprovedAt(tossResponse.getApprovedAt())
        );

        // 예약 확정
        payment.getReservation().confirm();

        return PaymentConfirmResponse.from(payment);
    }

    /**
     * 결제 취소 (환불)
     * - DONE 결제: Toss cancel API 호출 후 Payment CANCELLED
     * - PENDING 결제 (미승인): Toss 호출 없이 로컬 취소
     * - Reservation CANCELLED
     */
    @Override
    @Transactional
    public void cancelPayment(Long reservationId, PaymentCancelRequest request, Long memberId) {
        Reservation reservation = findReservationAndCheckOwner(reservationId, memberId);

        if (!reservation.isCancellable()) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELLABLE);
        }

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // Toss 승인이 완료된 결제만 Toss cancel API 호출
        if (payment.getStatus() == PaymentStatus.DONE) {
            callTossCancelApi(payment.getPaymentKey(), request);
        }

        payment.cancel();
        reservation.cancel();
    }

    /**
     * 결제 조회
     */
    @Override
    public PaymentResponse getPayment(Long reservationId, Long memberId) {
        findReservationAndCheckOwner(reservationId, memberId);

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);
    }

    // === Private 헬퍼 메서드 ===

    /** 예약 조회 + 본인 소유 검증 (타인 예약 접근 시 404) */
    private Reservation findReservationAndCheckOwner(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 정보 노출 방지: 타인 예약에는 404 반환
        if (!reservation.getGuest().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservation;
    }

    /** Toss 결제 승인 API 호출 */
    private TossPaymentApiResponse callTossConfirmApi(TossConfirmRequest request) {
        Map<String, Object> body = Map.of(
                "paymentKey", request.getPaymentKey(),
                "orderId", request.getOrderId(),
                "amount", request.getAmount()
        );

        try {
            return tossWebClient.post()
                    .uri(TOSS_CONFIRM_PATH)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(TossPaymentApiResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new BusinessException(ErrorCode.TOSS_API_ERROR);
        }
    }

    /** Toss 결제 취소 API 호출 */
    private void callTossCancelApi(String paymentKey, PaymentCancelRequest request) {
        // cancelAmount가 null이면 body에 포함하지 않아 전액 취소
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cancelReason", request.getCancelReason());
        if (request.getCancelAmount() != null) {
            body.put("cancelAmount", request.getCancelAmount());
        }

        try {
            tossWebClient.post()
                    .uri(TOSS_CANCEL_PATH, paymentKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(TossPaymentApiResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new BusinessException(ErrorCode.TOSS_API_ERROR);
        }
    }

    /** Toss method 한글 → PaymentMethod enum 변환 */
    private PaymentMethod mapTossMethod(String method) {
        if (method == null) return null;
        return switch (method) {
            case "카드" -> PaymentMethod.CARD;
            case "가상계좌" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "간편결제" -> PaymentMethod.EASY_PAY;
            case "계좌이체" -> PaymentMethod.BANK_TRANSFER;
            case "휴대폰" -> PaymentMethod.MOBILE_PHONE;
            default -> null;
        };
    }

    /** Toss approvedAt(ISO 8601 with offset) → LocalDateTime 변환 */
    private LocalDateTime parseApprovedAt(String approvedAt) {
        if (approvedAt == null) return null;
        return OffsetDateTime.parse(approvedAt).toLocalDateTime();
    }

    /** 결제창 표시 주문명 생성 */
    private String buildOrderName(Reservation reservation) {
        String accommodationName = reservation.getRoom().getAccommodation().getName();
        return accommodationName + " ("
                + reservation.getCheckInDate() + " ~ "
                + reservation.getCheckOutDate() + ")";
    }
}
