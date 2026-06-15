package com.roastedbear.stayhub.domain.member.entity;

/**
 * 회원 계정 상태
 */
public enum MemberStatus {
    ACTIVE,     // 정상
    SUSPENDED,  // 정지
    DELETED     // 탈퇴 (소프트 삭제)
}
