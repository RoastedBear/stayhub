package com.roastedbear.stayhub.domain.accommodation.entity;

import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 숙소 엔티티
 * - 호스트가 등록하는 숙소 단위 (호텔/펜션/모텔 등)
 * - 하나의 숙소 안에 여러 객실(Room)이 존재
 * - sido/sigungu 컬럼으로 지역 필터링 지원 (QueryDSL)
 */
@Entity
@Table(name = "accommodations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accommodation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 숙소를 등록한 호스트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private Member host;

    // 숙소명
    @Column(nullable = false, length = 100)
    private String name;

    // 숙소 소개
    @Column(columnDefinition = "TEXT")
    private String description;

    // 숙소 유형 (HOTEL, PENSION 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccommodationType type;

    // 시/도 (검색 필터용 - QueryDSL eq)
    @Column(nullable = false, length = 50)
    private String sido;

    // 시/군/구
    @Column(nullable = false, length = 50)
    private String sigungu;

    // 상세 주소
    @Column(nullable = false, length = 200)
    private String address;

    // 위도 (지도 표시용)
    @Column
    private Double latitude;

    // 경도 (지도 표시용)
    @Column
    private Double longitude;

    // 숙소 운영 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccommodationStatus status = AccommodationStatus.ACTIVE;

    @Builder
    public Accommodation(Member host, String name, String description,
                         AccommodationType type, String sido, String sigungu,
                         String address, Double latitude, Double longitude) {
        this.host = host;
        this.name = name;
        this.description = description;
        this.type = type;
        this.sido = sido;
        this.sigungu = sigungu;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = AccommodationStatus.ACTIVE;
    }

    // === 비즈니스 메서드 ===

    /** 숙소 정보 수정 */
    public void update(String name, String description, String address,
                       Double latitude, Double longitude) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** 숙소 비활성화 */
    public void deactivate() {
        this.status = AccommodationStatus.INACTIVE;
    }

    /** 숙소 삭제 (소프트 삭제) */
    public void delete() {
        this.status = AccommodationStatus.DELETED;
    }
}
