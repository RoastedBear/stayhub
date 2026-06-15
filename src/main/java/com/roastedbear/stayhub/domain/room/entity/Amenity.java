package com.roastedbear.stayhub.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 편의시설 마스터 엔티티
 * - Wi-Fi, 주차장, 조식 등 공통 편의시설 목록 관리
 * - 관리자가 미리 등록해두면 호스트가 객실에 연결하는 방식
 */
@Entity
@Table(name = "amenities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 편의시설명 (Wi-Fi, 주차장 등)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 카테고리 (BASIC, KITCHEN 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmenityCategory category;

    // 아이콘 이미지 URL
    @Column
    private String iconUrl;

    @Builder
    public Amenity(String name, AmenityCategory category, String iconUrl) {
        this.name = name;
        this.category = category;
        this.iconUrl = iconUrl;
    }
}
