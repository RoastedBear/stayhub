package com.roastedbear.stayhub.presentation.payment;

import com.roastedbear.stayhub.domain.payment.dto.PaymentCancelRequest;
import com.roastedbear.stayhub.domain.payment.dto.PaymentConfirmResponse;
import com.roastedbear.stayhub.domain.payment.dto.PaymentPrepareResponse;
import com.roastedbear.stayhub.domain.payment.dto.PaymentResponse;
import com.roastedbear.stayhub.domain.payment.dto.TossConfirmRequest;
import com.roastedbear.stayhub.domain.payment.service.PaymentService;
import com.roastedbear.stayhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 결제 API 컨트롤러 (전체 인증 필요)
 *
 * POST /api/payments/prepare/{reservationId}  - 결제 준비 (orderId 발급)
 * POST /api/payments/confirm                  - 결제 승인 (Toss 콜백 처리)
 * POST /api/payments/{reservationId}/cancel   - 결제 취소 (환불)
 * GET  /api/payments/{reservationId}          - 결제 조회
 *
 * Toss Payments 결제 플로우:
 *   1. POST prepare → orderId, amount 수신
 *   2. 프론트: Toss 결제창 호출 (clientKey, orderId, amount, orderName)
 *   3. 사용자: 결제 완료 → Toss successUrl로 리다이렉트 (paymentKey, orderId, amount)
 *   4. POST confirm → 결제 승인 완료
 */
@Tag(name = "Payment", description = "결제 API (Toss Payments)")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비
     * POST /api/payments/prepare/{reservationId}
     *
     * - Payment(PENDING) 레코드 생성 (없으면)
     * - Toss 결제창 초기화에 필요한 orderId, amount, orderName 반환
     */
    @Operation(
            summary = "결제 준비",
            description = """
                    예약에 대한 결제를 준비합니다.
                    - Payment(PENDING) 레코드를 생성하고 Toss 결제창 초기화에 필요한 정보를 반환합니다.
                    - 이미 완료된 결제가 있으면 409 오류를 반환합니다.
                    - 멱등성 보장: 동일 예약의 PENDING 결제가 있으면 기존 orderId를 재사용합니다.
                    """
    )
    @PostMapping("/prepare/{reservationId}")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "예약 ID") @PathVariable Long reservationId) {
        PaymentPrepareResponse response = paymentService.preparePayment(
                reservationId, userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 승인
     * POST /api/payments/confirm
     *
     * - Toss 결제창 완료 후 프론트에서 호출
     * - Toss /v1/payments/confirm API 호출
     * - 금액 위변조 검증: 서버 저장 금액 vs Toss 콜백 금액
     * - Reservation 상태 CONFIRMED 변경
     */
    @Operation(
            summary = "결제 승인",
            description = """
                    Toss 결제창 완료 후 결제를 승인합니다.
                    - Toss 서버에 결제 승인 요청을 보내고 결과를 저장합니다.
                    - 서버 저장 금액과 요청 금액이 다르면 400 오류를 반환합니다.
                    - 승인 성공 시 예약 상태가 CONFIRMED로 변경됩니다.
                    """
    )
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TossConfirmRequest request) {
        PaymentConfirmResponse response = paymentService.confirmPayment(
                request, userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 취소 (환불)
     * POST /api/payments/{reservationId}/cancel
     *
     * - DONE 결제: Toss cancel API 호출 후 환불
     * - PENDING 결제 (미승인): Toss 호출 없이 로컬 취소
     * - Reservation 상태 CANCELLED 변경
     */
    @Operation(
            summary = "결제 취소 (환불)",
            description = """
                    결제를 취소하고 환불을 요청합니다.
                    - cancelAmount를 지정하면 부분 취소, 미지정 시 전액 취소합니다.
                    - COMPLETED(이용 완료) 상태의 예약은 취소할 수 없습니다.
                    """
    )
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "예약 ID") @PathVariable Long reservationId,
            @Valid @RequestBody PaymentCancelRequest request) {
        paymentService.cancelPayment(reservationId, request, userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 결제 조회
     * GET /api/payments/{reservationId}
     */
    @Operation(summary = "결제 조회", description = "예약 ID로 결제 정보를 조회합니다.")
    @GetMapping("/{reservationId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "예약 ID") @PathVariable Long reservationId) {
        PaymentResponse response = paymentService.getPayment(
                reservationId, userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }
}
