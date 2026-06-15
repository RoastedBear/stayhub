package com.roastedbear.stayhub.domain.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 객실 검색 조건 DTO
 * - 모든 필드는 Optional: null이면 해당 조건 무시 (동적 쿼리)
 * - QueryDSL RoomRepositoryImpl에서 BooleanBuilder로 조합됨
 */
@Getter
@Setter
@NoArgsConstructor
public class RoomSearchCondition {

    // 지역 필터
    private String sido;        // 시/도 (예: "서울특별시")
    private String sigungu;     // 시/군/구 (예: "강남구")

    // 날짜 필터 - 두 값 모두 있을 때만 가용성 체크
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    // 인원 필터 - room.maxOccupancy >= guestCount
    private Integer guestCount;

    // 가격 범위 필터
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
