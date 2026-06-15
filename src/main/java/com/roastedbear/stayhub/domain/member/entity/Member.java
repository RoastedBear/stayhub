package com.roastedbear.stayhub.domain.member.entity;

import com.roastedbear.stayhub.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 회원 엔티티
 * - 호스트와 게스트 역할을 동시에 보유할 수 있음 (Airbnb 방식)
 * - roles는 ElementCollection으로 member_roles 테이블에 저장
 */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일 (로그인 ID, 고유값)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // BCrypt 암호화된 비밀번호
    @Column(nullable = false)
    private String password;

    // 실명
    @Column(nullable = false, length = 50)
    private String name;

    // 전화번호
    @Column(length = 20)
    private String phone;

    // 프로필 이미지 S3 URL
    @Column
    private String profileImageUrl;

    // 역할 (GUEST, HOST, ADMIN) - 복수 역할 보유 가능
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "member_roles",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<MemberRole> roles = new HashSet<>();

    // 계정 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Builder
    public Member(String email, String password, String name, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.roles.add(MemberRole.GUEST); // 기본 역할: GUEST
        this.status = MemberStatus.ACTIVE;
    }

    // === 비즈니스 메서드 ===

    /** 호스트 역할 추가 */
    public void addHostRole() {
        this.roles.add(MemberRole.HOST);
    }

    /** 비밀번호 변경 */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 프로필 이미지 변경 */
    public void updateProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }

    /** 회원 탈퇴 (소프트 삭제) */
    public void withdraw() {
        this.status = MemberStatus.DELETED;
    }

    /** 호스트 역할 보유 여부 확인 */
    public boolean isHost() {
        return this.roles.contains(MemberRole.HOST);
    }
}
