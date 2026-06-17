package com.roastedbear.stayhub.domain.member.service;

import com.roastedbear.stayhub.domain.member.dto.LoginRequest;
import com.roastedbear.stayhub.domain.member.dto.SignUpRequest;
import com.roastedbear.stayhub.domain.member.dto.TokenResponse;
import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.member.entity.MemberStatus;
import com.roastedbear.stayhub.domain.member.repository.MemberRepository;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import com.roastedbear.stayhub.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 회원 서비스 구현체
 *
 * Refresh Token 저장 전략 (Redis):
 *   - Key:   "RT:{memberId}"
 *   - Value: refreshToken 문자열
 *   - TTL:   jwt.refresh-token-expiry (ms → 초 변환)
 *
 * Token Rotation:
 *   - reissue 시 Access Token + Refresh Token 모두 새로 발급
 *   - 이전 Refresh Token은 Redis에서 즉시 교체됨
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "RT:";
    // Access Token 만료 시간 (초 단위) - TokenResponse.expiresIn에 사용
    private static final long ACCESS_TOKEN_EXPIRY_SEC = 3600L;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /**
     * 회원가입
     * 1. 이메일 중복 체크
     * 2. 비밀번호 암호화
     * 3. 회원 저장 (기본 역할: GUEST)
     * 4. 즉시 토큰 발급
     */
    @Override
    @Transactional
    public TokenResponse signUp(SignUpRequest request) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화 후 회원 생성
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        memberRepository.save(member);

        // 가입 즉시 로그인 상태 (토큰 발급)
        return issueTokens(member);
    }

    /**
     * 로그인
     * 1. 이메일로 회원 조회
     * 2. 비밀번호 검증 (BCrypt)
     * 3. 계정 상태 확인
     * 4. 토큰 발급 + Refresh Token Redis 저장
     */
    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 계정 상태 확인
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.SUSPENDED_ACCOUNT);
        }
        if (member.getStatus() == MemberStatus.DELETED) {
            throw new BusinessException(ErrorCode.DELETED_ACCOUNT);
        }

        return issueTokens(member);
    }

    /**
     * Access Token 재발급 (Refresh Token Rotation)
     * 1. Refresh Token 검증
     * 2. memberId 추출 → Redis 저장 토큰과 비교
     * 3. 새 토큰 발급
     */
    @Override
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // Refresh Token 유효성 검증 (만료/변조 체크)
        jwtProvider.validateToken(refreshToken);

        Long memberId = jwtProvider.getMemberId(refreshToken);
        String redisKey = REFRESH_TOKEN_KEY_PREFIX + memberId;

        // Redis에 저장된 Refresh Token과 비교
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (!storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return issueTokens(member);
    }

    /**
     * 로그아웃
     * - Redis에서 Refresh Token 삭제
     */
    @Override
    @Transactional
    public void logout(Long memberId) {
        String redisKey = REFRESH_TOKEN_KEY_PREFIX + memberId;
        redisTemplate.delete(redisKey);
    }

    /**
     * 토큰 발급 + Refresh Token Redis 저장 (내부 공통 메서드)
     */
    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRoles());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        // Refresh Token Redis 저장 (TTL: ms → Duration)
        String redisKey = REFRESH_TOKEN_KEY_PREFIX + member.getId();
        redisTemplate.opsForValue().set(redisKey, refreshToken, Duration.ofMillis(refreshTokenExpiry));

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(ACCESS_TOKEN_EXPIRY_SEC)
                .build();
    }
}
