package com.roastedbear.stayhub.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 승인 요청 DTO
 * - Toss 결제창 완료 후 프론트에서 서버로 전달하는 파라미터
 * - Toss 콜백 파라미터 3종: paymentKey, orderId, amount
 */
@Getter
@NoArgsConstructor
public class TossConfirmRequest {

    /** Toss가 발급한 결제 키 */
    @NotBlank(message = "paymentKey는 필수입니다.")
    private String paymentKey;

    /** 우리 서버에서 생성한 주문 ID */
    @NotBlank(message = "orderId는 필수입니다.")
    private String orderId;

    /** 결제 금액 (원 단위, Toss 측과 검증 필요) */
    @NotNull(message = "amount는 필수입니다.")
    @Positive(message = "amount는 양수여야 합니다.")
    private Long amount;
}
