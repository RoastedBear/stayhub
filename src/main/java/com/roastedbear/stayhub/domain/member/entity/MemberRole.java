package com.roastedbear.stayhub.domain.member.entity;

/**
 * 회원 역할 (한 사람이 GUEST + HOST 동시 보유 가능)
 */
public enum MemberRole {
    GUEST,  // 게스트 - 예약 가능
    HOST,   // 호스트 - 숙소 등록 가능
    ADMIN   // 관리자
}
