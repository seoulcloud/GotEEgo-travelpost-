package com.goteego.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 선호도 엔티티
 * 여행 스타일, 성격, 취향 등을 저장
 */
@Entity
@Table(name = "user_prefer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserPrefer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 🍶 술 관련 선호도
    @Column(name = "is_alchol3")
    private Boolean isAlchol3; // 술 좋아해요

    @Column(name = "is_alchol2")
    private Boolean isAlchol2; // 분위기상 한두 잔 정도

    @Column(name = "is_alchol1")
    private Boolean isAlchol1; // 술은 즐기지 않아요

    // 🚬 흡연 여부
    @Column(name = "is_smoker")
    private Boolean isSmoker;

    // 🤝 성격 관련
    @Column(name = "is_friendly")
    private Boolean isFriendly; // 새로운 사람과도 금방 친해져요

    @Column(name = "is_quiet")
    private Boolean isQuiet; // 조용한 분위기를 좋아해요

    @Column(name = "is_lead")
    private Boolean isLead; // 앞장서서 리드하는 편이에요

    @Column(name = "is_party")
    private Boolean isParty; // 분위기를 띄우는 걸 좋아해요

    @Column(name = "is_search")
    private Boolean isSearch; // 여행 중에도 정보를 꼼꼼히 찾는 편이에요

    @Column(name = "is_listen")
    private Boolean isListen; // 다른 사람 의견을 잘 들어주는 편이에요

    // 🏞 여행 활동 선호도
    @Column(name = "is_see")
    private Boolean isSee; // 자연 경관 감상

    @Column(name = "is_cafe")
    private Boolean isCafe; // 카페/휴식

    @Column(name = "is_taste")
    private Boolean isTaste; // 맛집 탐방

    @Column(name = "is_picture")
    private Boolean isPicture; // 사진 촬영

    @Column(name = "is_shopping")
    private Boolean isShopping; // 쇼핑

    @Column(name = "is_outdoor")
    private Boolean isOutdoor; // 액티비티(서핑 등산 등)

    // 🕘 여행 스타일
    @Column(name = "is_chill")
    private Boolean isChill; // 느긋하게 여유롭게

    @Column(name = "is_busy")
    private Boolean isBusy; // 빡빡하고 알차게

    @Column(name = "is_flex")
    private Boolean isFlex; // 상황에 따라 유동적으로

    // 🌆 여행지 선호도
    @Column(name = "is_city")
    private Boolean isCity; // 도시/핫플 위주

    @Column(name = "is_heal")
    private Boolean isHeal; // 자연/힐링 위주

    @Column(name = "is_beach")
    private Boolean isBeach; // 바다/해변

    @Column(name = "is_mountain")
    private Boolean isMountain; // 산/등산

    // 📅 생성/수정 시간
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    // User와의 관계
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 선호도 배열을 반환 (pgvector용)
     * 모든 boolean 값을 0,1로 변환하여 배열로 반환
     */
    public int[] getPreferenceArray() {
        return new int[]{
            booleanToInt(isAlchol3), booleanToInt(isAlchol2), booleanToInt(isAlchol1),
            booleanToInt(isSmoker), booleanToInt(isFriendly), booleanToInt(isQuiet),
            booleanToInt(isLead), booleanToInt(isParty), booleanToInt(isSearch),
            booleanToInt(isListen), booleanToInt(isSee), booleanToInt(isCafe),
            booleanToInt(isTaste), booleanToInt(isPicture), booleanToInt(isShopping),
            booleanToInt(isOutdoor), booleanToInt(isChill), booleanToInt(isBusy),
            booleanToInt(isFlex), booleanToInt(isCity), booleanToInt(isHeal),
            booleanToInt(isBeach), booleanToInt(isMountain)
        };
    }

    private int booleanToInt(Boolean value) {
        return value != null && value ? 1 : 0;
    }
} 