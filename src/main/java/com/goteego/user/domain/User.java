package com.goteego.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Embedded
    private OauthInfo oauthInfo;

    private boolean isSuspended = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    private String refreshToken;

    @Builder
    public User(String nickname, String profileImgUrl, UserRole role, OauthInfo oauthInfo, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
        this.oauthInfo = oauthInfo;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static User createDefaultOAuthUser(OauthInfo oauthInfo, String profileImgUrl) {
        return User.builder()
                .nickname(oauthInfo.getName())
                .profileImgUrl(profileImgUrl)
                .role(UserRole.USER)
                .oauthInfo(oauthInfo)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setIsSuspend(boolean isSuspended) {
        this.isSuspended = isSuspended;
        this.modifiedAt = LocalDateTime.now();
    }
} 