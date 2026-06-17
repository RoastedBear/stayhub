package com.roastedbear.stayhub.global.security;

import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.member.entity.MemberStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetails 구현체
 * - Member 엔티티를 감싸서 인증 정보 제공
 * - roles → "ROLE_GUEST", "ROLE_HOST" 형식의 GrantedAuthority 변환
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Member member;

    /** 회원 ID 반환 (JWT subject 파싱 후 조회에 사용) */
    public Long getMemberId() {
        return member.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // MemberRole enum → "ROLE_{ROLE_NAME}" 형식으로 변환
        return member.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    // 계정 만료 여부 (stayhub는 만료 정책 없음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부 (SUSPENDED → 잠김)
    @Override
    public boolean isAccountNonLocked() {
        return member.getStatus() != MemberStatus.SUSPENDED;
    }

    // 자격증명 만료 여부 (없음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 (DELETED → 비활성)
    @Override
    public boolean isEnabled() {
        return member.getStatus() == MemberStatus.ACTIVE;
    }
}
