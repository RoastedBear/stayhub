package com.roastedbear.stayhub.domain.member.service;

import com.roastedbear.stayhub.domain.member.dto.LoginRequest;
import com.roastedbear.stayhub.domain.member.dto.SignUpRequest;
import com.roastedbear.stayhub.domain.member.dto.TokenResponse;

/**
 * 회원 서비스 인터페이스
 */
public interface MemberService {

    /**
     * 회원가입
     * - 이메일 중복 체크
     * - 비밀번호 BCrypt 암호화
     * - 가입 즉시 토큰 발급 (로그인 불필요)
     */
    TokenResponse signUp(SignUpRequest request);

    /**
     * 로그인
     * - 이메일/비밀번호 검증
     * - Access Token + Refresh Token 발급
     * - Refresh Token은 Redis에 저장
     */
    TokenResponse login(LoginRequest request);

    /**
     * Access Token 재발급
     * - Refresh Token 검증
     * - Redis에 저장된 토큰과 일치 여부 확인
     * - 새 Access Token + Refresh Token 발급 (Rotation)
     */
    TokenResponse reissue(String refreshToken);

    /**
     * 이메일 중복 확인
     * - true: 사용 가능, false: 이미 사용 중
     */
    boolean isEmailAvailable(String email);

    /**
     * 로그아웃
     * - Redis에서 Refresh Token 삭제
     */
    void logout(Long memberId);
}
