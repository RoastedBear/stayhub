package com.roastedbear.stayhub.domain.room.entity;

import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 객실 엔티티
 * - 예약과 검색의 핵심 단위
 * - Redis 분산 락의 대상: "room:{roomId}:date:{date}"
 * - 편의시설(Amenity)과 N:M 관계 (room_amenity 조인 테이블)
 */
@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 객실이 속한 숙소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    // 객실명 (예: 스탠다드 더블, 디럭스 트윈)
    @Column(nullable = false, length = 100)
    private String name;

    // 객실 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 최대 수용 인원
    @Column(nullable = false)
    private Integer maxOccupancy;

    // 1박 기준 기본 가격
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // 객실 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.AVAILABLE;

    // 편의시설 목록 (N:M → room_amenity 조인 테이블)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_amenity",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities = new ArrayList<>();

    @Builder
    public Room(Accommodation accommodation, String name, String description,
                Integer maxOccupancy, BigDecimal basePrice) {
        this.accommodation = accommodation;
        this.name = name;
        this.description = description;
        this.maxOccupancy = maxOccupancy;
        this.basePrice = basePrice;
        this.status = RoomStatus.AVAILABLE;
    }

    // === 비즈니스 메서드 ===

    /** 객실 정보 수정 */
    public void update(String name, String description,
                       Integer maxOccupancy, BigDecimal basePrice) {
        this.name = name;
        this.description = description;
        this.maxOccupancy = maxOccupancy;
        this.basePrice = basePrice;
    }

    /** 편의시설 추가 */
    public void addAmenity(Amenity amenity) {
        this.amenities.add(amenity);
    }

    /** 편의시설 전체 교체 */
    public void updateAmenities(List<Amenity> amenities) {
        this.amenities.clear();
        this.amenities.addAll(amenities);
    }

    /** 객실 비활성화 */
    public void deactivate() {
        this.status = RoomStatus.UNAVAILABLE;
    }

    /** 객실 활성화 */
    public void activate() {
        this.status = RoomStatus.AVAILABLE;
    }
}
