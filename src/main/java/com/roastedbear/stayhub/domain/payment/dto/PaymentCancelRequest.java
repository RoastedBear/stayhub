package com.roastedbear.stayhub.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 취소(환불) 요청 DTO
 * - cancelAmount가 null이면 Toss 측에 전액 취소 요청
 */
@Getter
@NoArgsConstructor
public class PaymentCancelRequest {

    /** 취소 사유 (Toss API 필수값) */
    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;

    /** 부분 취소 금액 (null이면 전액 취소) */
    private Long cancelAmount;
}
