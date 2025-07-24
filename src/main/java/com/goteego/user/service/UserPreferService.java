package com.goteego.user.service;

import com.goteego.user.domain.UserPrefer;
import com.goteego.user.repository.UserPreferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 선호도 서비스
 * 선호도 CRUD 및 유사 사용자 검색 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPreferService {

    private final UserPreferRepository userPreferRepository;

    /**
     * 사용자 선호도 저장/수정
     */
    @Transactional
    public UserPrefer saveUserPrefer(UserPrefer userPrefer) {
        log.info("사용자 선호도 저장/수정: userId={}", userPrefer.getUserId());
        return userPreferRepository.save(userPrefer);
    }

    /**
     * 사용자 ID로 선호도 조회
     */
    public Optional<UserPrefer> getUserPreferByUserId(Long userId) {
        log.info("사용자 선호도 조회: userId={}", userId);
        return userPreferRepository.findByUserId(userId);
    }

    /**
     * 사용자 선호도 삭제
     */
    @Transactional
    public void deleteUserPrefer(Long userId) {
        log.info("사용자 선호도 삭제: userId={}", userId);
        userPreferRepository.deleteById(userId);
    }

    /**
     * 친화적인 사용자들 조회
     */
    public List<UserPrefer> getFriendlyUsers() {
        log.info("친화적인 사용자들 조회");
        return userPreferRepository.findByIsFriendlyTrue();
    }

    /**
     * 리더십 있는 사용자들 조회
     */
    public List<UserPrefer> getLeadUsers() {
        log.info("리더십 있는 사용자들 조회");
        return userPreferRepository.findByIsLeadTrue();
    }

    /**
     * 아웃도어 활동을 좋아하는 사용자들 조회
     */
    public List<UserPrefer> getOutdoorUsers() {
        log.info("아웃도어 활동을 좋아하는 사용자들 조회");
        return userPreferRepository.findByIsOutdoorTrue();
    }

    /**
     * 도시 여행을 선호하는 사용자들 조회
     */
    public List<UserPrefer> getCityUsers() {
        log.info("도시 여행을 선호하는 사용자들 조회");
        return userPreferRepository.findByIsCityTrue();
    }

    /**
     * 힐링 여행을 선호하는 사용자들 조회
     */
    public List<UserPrefer> getHealUsers() {
        log.info("힐링 여행을 선호하는 사용자들 조회");
        return userPreferRepository.findByIsHealTrue();
    }

    /**
     * 술을 좋아하는 사용자들 조회
     */
    public List<UserPrefer> getAlcoholLovers() {
        log.info("술을 좋아하는 사용자들 조회");
        return userPreferRepository.findByIsAlchol3True();
    }

    /**
     * 술을 마시지 않는 사용자들 조회
     */
    public List<UserPrefer> getNonAlcoholUsers() {
        log.info("술을 마시지 않는 사용자들 조회");
        return userPreferRepository.findByIsAlchol1True();
    }

    /**
     * 흡연자들 조회
     */
    public List<UserPrefer> getSmokers() {
        log.info("흡연자들 조회");
        return userPreferRepository.findByIsSmokerTrue();
    }

    /**
     * 비흡연자들 조회
     */
    public List<UserPrefer> getNonSmokers() {
        log.info("비흡연자들 조회");
        return userPreferRepository.findByIsSmokerFalse();
    }

    /**
     * 친화적이고 아웃도어 활동을 좋아하는 사용자들 조회
     */
    public List<UserPrefer> getFriendlyAndOutdoorUsers() {
        log.info("친화적이고 아웃도어 활동을 좋아하는 사용자들 조회");
        return userPreferRepository.findFriendlyAndOutdoorUsers();
    }

    /**
     * 리더십 있고 도시 여행을 선호하는 사용자들 조회
     */
    public List<UserPrefer> getLeadAndCityUsers() {
        log.info("리더십 있고 도시 여행을 선호하는 사용자들 조회");
        return userPreferRepository.findLeadAndCityUsers();
    }

    /**
     * 여유롭고 힐링 여행을 선호하는 사용자들 조회
     */
    public List<UserPrefer> getChillAndHealUsers() {
        log.info("여유롭고 힐링 여행을 선호하는 사용자들 조회");
        return userPreferRepository.findChillAndHealUsers();
    }

    /**
     * 활발한 여행 스타일을 가진 사용자들 조회
     */
    public List<UserPrefer> getActiveTravelStyleUsers() {
        log.info("활발한 여행 스타일을 가진 사용자들 조회");
        return userPreferRepository.findActiveTravelStyleUsers();
    }

    /**
     * 문화 활동을 선호하는 사용자들 조회
     */
    public List<UserPrefer> getCulturalActivityUsers() {
        log.info("문화 활동을 선호하는 사용자들 조회");
        return userPreferRepository.findCulturalActivityUsers();
    }

    /**
     * 특정 사용자와 유사한 선호도를 가진 사용자들 조회
     */
    public List<UserPrefer> getSimilarUsers(UserPrefer targetUserPrefer) {
        log.info("유사한 선호도를 가진 사용자들 조회: userId={}", targetUserPrefer.getUserId());
        
        // 기본적인 유사성 기준으로 필터링
        List<UserPrefer> similarUsers = userPreferRepository.findAll();
        
        // 자기 자신 제외
        similarUsers.removeIf(userPrefer -> userPrefer.getUserId().equals(targetUserPrefer.getUserId()));
        
        // 선호도 유사도 점수 계산 및 정렬
        similarUsers.sort((u1, u2) -> {
            double score1 = calculateSimilarityScore(targetUserPrefer, u1);
            double score2 = calculateSimilarityScore(targetUserPrefer, u2);
            return Double.compare(score2, score1); // 내림차순 정렬
        });
        
        return similarUsers;
    }

    /**
     * 두 사용자의 선호도 유사도 점수 계산
     */
    private double calculateSimilarityScore(UserPrefer user1, UserPrefer user2) {
        int[] preferences1 = user1.getPreferenceArray();
        int[] preferences2 = user2.getPreferenceArray();
        
        int matchCount = 0;
        int totalCount = preferences1.length;
        
        for (int i = 0; i < totalCount; i++) {
            if (preferences1[i] == preferences2[i]) {
                matchCount++;
            }
        }
        
        return (double) matchCount / totalCount;
    }

    /**
     * 사용자 선호도 존재 여부 확인
     */
    public boolean existsByUserId(Long userId) {
        return userPreferRepository.existsById(userId);
    }
} 