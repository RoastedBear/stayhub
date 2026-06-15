package com.roastedbear.stayhub.domain.review.entity;

import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.reservation.entity.Reservation;
import com.roastedbear.stayhub.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 엔티티
 * - 체크아웃 완료된 예약(COMPLETED)에 대해서만 작성 가능
 * - 예약 1건당 리뷰 1건 (1:1 관계)
 * - accommodation FK를 직접 보유하여 숙소별 평점 집계 쿼리 최적화
 */
@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리뷰 대상 예약 (예약당 리뷰 1건)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    // 리뷰 작성자 (게스트)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Member guest;

    // 숙소 (평점 집계를 위해 직접 FK 보유)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    // 평점 (1 ~ 5)
    @Column(nullable = false)
    private Integer rating;

    // 리뷰 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public Review(Reservation reservation, Member guest,
                  Accommodation accommodation, Integer rating, String content) {
        this.reservation = reservation;
        this.guest = guest;
        this.accommodation = accommodation;
        this.rating = rating;
        this.content = content;
    }

    // === 비즈니스 메서드 ===

    /** 리뷰 수정 */
    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
