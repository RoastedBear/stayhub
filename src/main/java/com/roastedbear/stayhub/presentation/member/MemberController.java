package com.roastedbear.stayhub.presentation.member;

import com.roastedbear.stayhub.domain.member.dto.LoginRequest;
import com.roastedbear.stayhub.domain.member.dto.SignUpRequest;
import com.roastedbear.stayhub.domain.member.dto.TokenResponse;
import com.roastedbear.stayhub.domain.member.service.MemberService;
import com.roastedbear.stayhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 회원 인증 API 컨트롤러
 * - 모든 경로는 SecurityConfig에서 인증 없이 허용 (단, /logout은 인증 필요)
 */
@Tag(name = "Auth", description = "회원 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 이메일 중복 확인
     * GET /api/auth/check-email?email={email}
     */
    @Operation(summary = "이메일 중복 확인", description = "available: true = 사용 가능, false = 이미 사용 중")
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = memberService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @Operation(summary = "회원가입", description = "이메일/비밀번호로 회원가입. 가입 즉시 토큰 발급.")
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        TokenResponse response = memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @Operation(summary = "로그인", description = "이메일/비밀번호 로그인. Access Token + Refresh Token 발급.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = memberService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Access Token 재발급
     * POST /api/auth/reissue
     * - Header: Refresh-Token: {refreshToken}
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token + Refresh Token 재발급 (Token Rotation).")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponse response = memberService.reissue(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * - 인증 필요: Authorization: Bearer {accessToken}
     * - Redis에서 Refresh Token 삭제
     */
    @Operation(summary = "로그아웃", description = "Redis의 Refresh Token 삭제.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        memberService.logout(userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
