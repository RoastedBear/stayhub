package com.roastedbear.stayhub.domain.reservation.entity;

import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.room.entity.Room;
import com.roastedbear.stayhub.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 예약 엔티티
 * - Redis 분산 락으로 동시성 제어: 같은 객실의 같은 날짜 중복 예약 방지
 * - 락 키 형식: "lock:reservation:room:{roomId}:{checkInDate}"
 * - PENDING → CONFIRMED (결제 완료) → COMPLETED (체크아웃)
 *                     ↓ 취소
 *                  CANCELLED
 */
@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약된 객실
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // 예약한 게스트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Member guest;

    // 체크인 날짜
    @Column(nullable = false)
    private LocalDate checkInDate;

    // 체크아웃 날짜
    @Column(nullable = false)
    private LocalDate checkOutDate;

    // 투숙 인원
    @Column(nullable = false)
    private Integer guestCount;

    // 최종 결제 금액 (basePrice * 박수)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // 예약 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Builder
    public Reservation(Room room, Member guest, LocalDate checkInDate,
                       LocalDate checkOutDate, Integer guestCount, BigDecimal totalPrice) {
        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.PENDING;
    }

    // === 비즈니스 메서드 ===

    /** 결제 완료 → 예약 확정 */
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    /** 예약 취소 */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    /** 이용 완료 (체크아웃 후 배치 처리) */
    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    /** 취소 가능 여부 확인 */
    public boolean isCancellable() {
        return this.status == ReservationStatus.PENDING
                || this.status == ReservationStatus.CONFIRMED;
    }
}
