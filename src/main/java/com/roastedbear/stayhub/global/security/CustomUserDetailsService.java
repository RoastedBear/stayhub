package com.roastedbear.stayhub.global.security;

import com.roastedbear.stayhub.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체
 * - JwtAuthenticationFilter에서 memberId로 UserDetails 로드 시 사용
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * username = memberId (String)
     * JWT subject에 memberId를 저장하므로 ID로 조회
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        return memberRepository.findById(Long.parseLong(memberId))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId));
    }
}
