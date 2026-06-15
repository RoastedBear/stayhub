package com.roastedbear.stayhub.domain.accommodation.entity;

/**
 * 숙소 상태
 */
public enum AccommodationStatus {
    ACTIVE,   // 운영중
    INACTIVE, // 운영 중지 (호스트가 비활성화)
    DELETED   // 삭제 (소프트 삭제)
}
