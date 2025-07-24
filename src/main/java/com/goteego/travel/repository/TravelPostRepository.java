package com.goteego.travel.repository;

import com.goteego.travel.domain.TravelPost;
import com.goteego.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 여행 게시글 Repository
 */
@Repository
public interface TravelPostRepository extends JpaRepository<TravelPost, Long> {

    /**
     * 기본 조회 메서드
     */
    Optional<TravelPost> findByTravelPostId(Long travelPostId);
    
    /**
     * 작성자별 게시글 조회
     */
    List<TravelPost> findByUser(User user);
    Page<TravelPost> findByUser(User user, Pageable pageable);
    
    /**
     * 게시글 타입별 조회
     */
    List<TravelPost> findByPostType(TravelPost.PostType postType);
    Page<TravelPost> findByPostType(TravelPost.PostType postType, Pageable pageable);
    
    /**
     * 모집 상태별 조회
     */
    List<TravelPost> findByIsAddRecruit(Boolean isAddRecruit);
    Page<TravelPost> findByIsAddRecruit(Boolean isAddRecruit, Pageable pageable);
    
    /**
     * 제목으로 검색
     */
    List<TravelPost> findByTitleContainingIgnoreCase(String title);
    Page<TravelPost> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * 내용으로 검색
     */
    List<TravelPost> findByContentContainingIgnoreCase(String content);
    Page<TravelPost> findByContentContainingIgnoreCase(String content, Pageable pageable);
    
    /**
     * 제목 또는 내용으로 검색
     */
    @Query("SELECT tp FROM TravelPost tp WHERE LOWER(tp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(tp.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TravelPost> findByTitleOrContentContainingIgnoreCase(@Param("keyword") String keyword);
    
    /**
     * 날짜 범위로 조회
     */
    List<TravelPost> findByStartTimeBetween(LocalDate startDate, LocalDate endDate);
    List<TravelPost> findByEndTimeBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 날짜 이후 시작하는 여행
     */
    List<TravelPost> findByStartTimeAfter(LocalDate date);
    
    /**
     * 특정 날짜 이전에 끝나는 여행
     */
    List<TravelPost> findByEndTimeBefore(LocalDate date);
    
    /**
     * 현재 진행 중인 여행 (오늘 날짜가 시작일과 종료일 사이)
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.startTime <= :today AND tp.endTime >= :today")
    List<TravelPost> findCurrentlyTravelingPosts(@Param("today") LocalDate today);
    
    /**
     * 예정된 여행 (시작일이 오늘 이후)
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.startTime > :today")
    List<TravelPost> findUpcomingTravelPosts(@Param("today") LocalDate today);
    
    /**
     * 복합 조건 검색
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.postType = :postType AND tp.isAddRecruit = :isAddRecruit")
    List<TravelPost> findByPostTypeAndRecruitmentStatus(
        @Param("postType") TravelPost.PostType postType,
        @Param("isAddRecruit") Boolean isAddRecruit
    );
    
    /**
     * 조회수 기준 정렬
     */
    @Query("SELECT tp FROM TravelPost tp ORDER BY tp.viewCount DESC")
    List<TravelPost> findAllOrderByViewCountDesc();
    
    /**
     * 최신순 정렬
     */
    @Query("SELECT tp FROM TravelPost tp ORDER BY tp.createdAt DESC")
    List<TravelPost> findAllOrderByCreatedAtDesc();
    
    /**
     * 여행 시작일 기준 정렬
     */
    @Query("SELECT tp FROM TravelPost tp ORDER BY tp.startTime ASC")
    List<TravelPost> findAllOrderByStartTimeAsc();
    
    /**
     * 특정 사용자가 작성한 게시글 중 모집 중인 것들
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.user = :user AND tp.isAddRecruit = false")
    List<TravelPost> findActivePostsByUser(@Param("user") User user);
    
    /**
     * 채팅방 ID로 게시글 조회
     */
    Optional<TravelPost> findByChatRoomId(Long chatRoomId);
    
    /**
     * 통계 쿼리
     */
    @Query("SELECT COUNT(tp) FROM TravelPost tp WHERE tp.postType = :postType")
    long countByPostType(@Param("postType") TravelPost.PostType postType);
    
    @Query("SELECT COUNT(tp) FROM TravelPost tp WHERE tp.isAddRecruit = :isAddRecruit")
    long countByRecruitmentStatus(@Param("isAddRecruit") Boolean isAddRecruit);
    
    @Query("SELECT AVG(tp.viewCount) FROM TravelPost tp")
    Double getAverageViewCount();

    /**
     * 특정 사용자가 참여한 여행 게시글 조회 (페이징)
     */
    @Query("SELECT tp FROM TravelPost tp JOIN ParticipationApplication pa ON tp.travelPostId = pa.travelPost.travelPostId WHERE pa.user.userId = :userId")
    Page<TravelPost> findByParticipantUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 사용자가 작성한 여행 게시글 조회 (페이징)
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.user.userId = :userId")
    Page<TravelPost> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 사용자가 작성한 여행 게시글 조회 (전체)
     */
    @Query("SELECT tp FROM TravelPost tp WHERE tp.user.userId = :userId")
    List<TravelPost> findAllByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 참여한 여행 게시글 조회 (전체)
     */
    List<TravelPost> findAllByParticipantUserId(Long userId);
} 