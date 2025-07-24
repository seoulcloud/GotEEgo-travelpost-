package com.goteego.user.repository;

import com.goteego.user.domain.UserPrefer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 선호도 Repository
 */
@Repository
public interface UserPreferRepository extends JpaRepository<UserPrefer, Long> {

    /**
     * 사용자 ID로 선호도 조회
     */
    Optional<UserPrefer> findByUserId(Long userId);

    /**
     * 특정 선호도를 가진 사용자들 조회
     */
    List<UserPrefer> findByIsFriendlyTrue();
    List<UserPrefer> findByIsLeadTrue();
    List<UserPrefer> findByIsOutdoorTrue();
    List<UserPrefer> findByIsCityTrue();
    List<UserPrefer> findByIsHealTrue();

    /**
     * 술 관련 선호도로 사용자 조회
     */
    List<UserPrefer> findByIsAlchol3True(); // 술 좋아하는 사람
    List<UserPrefer> findByIsAlchol1True(); // 술 안 마시는 사람

    /**
     * 흡연 여부로 사용자 조회
     */
    List<UserPrefer> findByIsSmokerTrue();  // 흡연자
    List<UserPrefer> findByIsSmokerFalse(); // 비흡연자

    /**
     * 복합 조건으로 사용자 조회
     */
    @Query("SELECT up FROM UserPrefer up WHERE up.isFriendly = true AND up.isOutdoor = true")
    List<UserPrefer> findFriendlyAndOutdoorUsers();

    @Query("SELECT up FROM UserPrefer up WHERE up.isLead = true AND up.isCity = true")
    List<UserPrefer> findLeadAndCityUsers();

    @Query("SELECT up FROM UserPrefer up WHERE up.isChill = true AND up.isHeal = true")
    List<UserPrefer> findChillAndHealUsers();

    /**
     * 특정 여행 스타일을 가진 사용자 조회
     */
    @Query("SELECT up FROM UserPrefer up WHERE up.isBusy = true OR up.isFlex = true")
    List<UserPrefer> findActiveTravelStyleUsers();

    /**
     * 특정 활동을 선호하는 사용자 조회
     */
    @Query("SELECT up FROM UserPrefer up WHERE up.isPicture = true OR up.isTaste = true OR up.isShopping = true")
    List<UserPrefer> findCulturalActivityUsers();
} 