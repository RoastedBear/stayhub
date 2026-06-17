package com.roastedbear.stayhub.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 모든 요청에서 1회 실행 (OncePerRequestFilter)
 * - Authorization 헤더에서 Bearer 토큰 추출 → 검증 → SecurityContext 설정
 *
 * 처리 흐름:
 *   1. "Authorization: Bearer {token}" 헤더 추출
 *   2. JwtProvider로 토큰 유효성 검증
 *   3. 토큰에서 memberId 추출 → UserDetails 로드
 *   4. SecurityContextHolder에 Authentication 설정
 *
 * 토큰이 없거나 유효하지 않으면 SecurityContext를 설정하지 않고 통과
 * → SecurityConfig의 authorizeHttpRequests에서 인증 필요 여부 최종 결정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
                Long memberId = jwtProvider.getMemberId(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(memberId));

                // SecurityContext에 인증 정보 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 토큰 오류 시 SecurityContext 초기화 후 다음 필터로 진행
            // → SecurityConfig에서 인증 필요 경로면 401 반환
            log.debug("JWT 인증 처리 중 오류: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     *
     * @return 토큰 문자열 (없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
