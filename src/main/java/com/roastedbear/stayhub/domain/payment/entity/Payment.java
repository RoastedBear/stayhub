package com.roastedbear.stayhub.domain.payment.entity;

import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 * - Toss Payments REST API 연동 정보를 저장
 * - 예약 1건당 결제 1건 (1:1 관계)
 * - paymentKey: Toss가 발급하는 결제 고유 키 (취소/조회 시 사용)
 * - orderId: 우리 서버에서 생성하는 주문 ID (UUID 권장)
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제에 연결된 예약 (예약당 결제 1건)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    // 결제한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Toss Payments 결제 키 (승인 후 발급, 취소 API 호출 시 필요)
    @Column(unique = true, length = 200)
    private String paymentKey;

    // 우리 서버에서 생성한 주문 ID (UUID)
    @Column(nullable = false, unique = true, length = 100)
    private String orderId;

    // 결제 금액
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // 결제 수단
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentMethod method;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    // Toss에서 결제 승인된 시각
    @Column
    private LocalDateTime approvedAt;

    // 결제 생성 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Payment(Reservation reservation, Member member,
                   String orderId, BigDecimal amount) {
        this.reservation = reservation;
        this.member = member;
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // === 비즈니스 메서드 ===

    /** Toss 결제 승인 완료 처리 */
    public void approve(String paymentKey, PaymentMethod method, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.status = PaymentStatus.DONE;
        this.approvedAt = approvedAt;
    }

    /** 결제 취소 */
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    /** 부분 취소 */
    public void partialCancel() {
        this.status = PaymentStatus.PARTIAL_CANCELLED;
    }

    /** 결제 실패 */
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
}
