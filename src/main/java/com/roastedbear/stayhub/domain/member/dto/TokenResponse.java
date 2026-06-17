package com.roastedbear.stayhub.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 발급 응답 DTO
 * - 회원가입 / 로그인 / 토큰 재발급 시 공통으로 사용
 */
@Getter
@Builder
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;

    // 클라이언트가 Authorization 헤더에 붙일 토큰 타입
    private final String tokenType = "Bearer";

    // Access Token 만료 시간 (초 단위)
    private final long expiresIn;
}
