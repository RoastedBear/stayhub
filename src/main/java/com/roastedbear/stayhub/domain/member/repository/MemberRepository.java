package com.roastedbear.stayhub.domain.member.repository;

import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.member.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 레포지토리
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 조회 (로그인, 중복 가입 체크)
    Optional<Member> findByEmail(String email);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // 이메일 + 상태로 조회 (로그인 시 탈퇴/정지 계정 필터링)
    Optional<Member> findByEmailAndStatus(String email, MemberStatus status);
}
