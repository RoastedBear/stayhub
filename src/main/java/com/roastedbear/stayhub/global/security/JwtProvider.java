package com.roastedbear.stayhub.global.security;

import com.roastedbear.stayhub.domain.member.entity.MemberRole;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 / 검증 / 파싱 컴포넌트
 *
 * Access Token:
 *   - subject: memberId (String)
 *   - claim "roles": Set<MemberRole> → "GUEST,HOST" 형식 문자열
 *   - 만료: 1시간 (application.yaml)
 *
 * Refresh Token:
 *   - subject: memberId (String)
 *   - claim 없음 (재발급 용도만)
 *   - 만료: 14일 (application.yaml)
 *   - Redis에 저장: "RT:{memberId}" → refreshToken (TTL = 14일)
 */
@Slf4j
@Component
public class JwtProvider {

    // application.yaml의 jwt.secret (Base64 인코딩된 256bit 이상 키)
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /** 서명 키 생성 */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     *
     * @param memberId 회원 ID
     * @param roles    회원 역할 Set
     * @return Bearer 토큰 문자열
     */
    public String createAccessToken(Long memberId, Set<MemberRole> roles) {
        // roles Set → 콤마 구분 문자열 (예: "GUEST,HOST")
        String rolesString = roles.stream()
                .map(MemberRole::name)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("roles", rolesString)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Refresh Token 생성 (역할 정보 없음, 재발급 전용)
     *
     * @param memberId 회원 ID
     * @return Refresh 토큰 문자열
     */
    public String createRefreshToken(Long memberId) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 토큰에서 회원 ID 추출
     *
     * @param token JWT 토큰
     * @return 회원 ID
     */
    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * 토큰 유효성 검증
     * - 만료, 서명 오류, 형식 오류 등 모두 체크
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * Claims 파싱 (내부 사용)
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Refresh Token 만료 시간(ms) 반환 - Redis TTL 설정에 사용 */
    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }
}
