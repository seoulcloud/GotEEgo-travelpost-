package com.goteego.travel.repository;

import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.TravelPost;
import com.goteego.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 참가 신청 Repository
 */
@Repository
public interface ParticipationApplicationRepository extends JpaRepository<ParticipationApplication, Long> {

    /**
     * 기본 조회 메서드
     */
    Optional<ParticipationApplication> findByParticipationApplicationId(Long participationApplicationId);
    
    /**
     * 사용자별 참가 신청 조회
     */
    List<ParticipationApplication> findByUser(User user);
    List<ParticipationApplication> findByUserOrderByRequestedAtDesc(User user);
    
    /**
     * 여행 게시글별 참가 신청 조회
     */
    List<ParticipationApplication> findByTravelPost(TravelPost travelPost);
    List<ParticipationApplication> findByTravelPostOrderByRequestedAtAsc(TravelPost travelPost);
    
    /**
     * 상태별 참가 신청 조회
     */
    List<ParticipationApplication> findByStatus(ParticipationApplication.ParticipationStatus status);
    List<ParticipationApplication> findByTravelPostAndStatus(TravelPost travelPost, ParticipationApplication.ParticipationStatus status);
    List<ParticipationApplication> findByUserAndStatus(User user, ParticipationApplication.ParticipationStatus status);
    
    /**
     * 특정 사용자가 특정 게시글에 신청한 내역 조회
     */
    Optional<ParticipationApplication> findByUserAndTravelPost(User user, TravelPost travelPost);
    
    /**
     * 특정 게시글의 승인된 참가자 수 조회
     */
    @Query("SELECT COUNT(pa) FROM ParticipationApplication pa WHERE pa.travelPost = :travelPost AND pa.status = 'APPROVED'")
    long countApprovedApplicationsByTravelPost(@Param("travelPost") TravelPost travelPost);
    
    /**
     * 특정 게시글의 대기 중인 신청 수 조회
     */
    @Query("SELECT COUNT(pa) FROM ParticipationApplication pa WHERE pa.travelPost = :travelPost AND pa.status = 'PENDING'")
    long countPendingApplicationsByTravelPost(@Param("travelPost") TravelPost travelPost);
    
    /**
     * 특정 사용자의 승인된 참가 신청 조회
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.user = :user AND pa.status = 'APPROVED'")
    List<ParticipationApplication> findApprovedApplicationsByUser(@Param("user") User user);
    
    /**
     * 특정 사용자의 대기 중인 참가 신청 조회
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.user = :user AND pa.status = 'PENDING'")
    List<ParticipationApplication> findPendingApplicationsByUser(@Param("user") User user);
    
    /**
     * 특정 사용자의 거절된 참가 신청 조회
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.user = :user AND pa.status = 'REJECTED'")
    List<ParticipationApplication> findRejectedApplicationsByUser(@Param("user") User user);
    
    /**
     * 게시글 작성자가 받은 참가 신청 조회 (최신순)
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.travelPost.user = :postAuthor ORDER BY pa.requestedAt DESC")
    List<ParticipationApplication> findApplicationsForPostAuthor(@Param("postAuthor") User postAuthor);
    
    /**
     * 게시글 작성자가 받은 대기 중인 참가 신청 조회
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.travelPost.user = :postAuthor AND pa.status = 'PENDING' ORDER BY pa.requestedAt ASC")
    List<ParticipationApplication> findPendingApplicationsForPostAuthor(@Param("postAuthor") User postAuthor);
    
    /**
     * 특정 게시글의 모든 참가 신청 (상태별 정렬)
     */
    @Query("SELECT pa FROM ParticipationApplication pa WHERE pa.travelPost = :travelPost ORDER BY " +
           "CASE pa.status WHEN 'PENDING' THEN 1 WHEN 'APPROVED' THEN 2 WHEN 'REJECTED' THEN 3 END, " +
           "pa.requestedAt ASC")
    List<ParticipationApplication> findAllByTravelPostOrderByStatusAndRequestedAt(@Param("travelPost") TravelPost travelPost);
    
    /**
     * 사용자가 승인된 여행 게시글 조회
     */
    @Query("SELECT pa.travelPost FROM ParticipationApplication pa WHERE pa.user = :user AND pa.status = 'APPROVED'")
    List<TravelPost> findApprovedTravelPostsByUser(@Param("user") User user);
    
    /**
     * 통계 쿼리
     */
    @Query("SELECT COUNT(pa) FROM ParticipationApplication pa WHERE pa.status = :status")
    long countByStatus(@Param("status") ParticipationApplication.ParticipationStatus status);
    
    @Query("SELECT COUNT(pa) FROM ParticipationApplication pa WHERE pa.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(pa) FROM ParticipationApplication pa WHERE pa.travelPost = :travelPost")
    long countByTravelPost(@Param("travelPost") TravelPost travelPost);
    
    /**
     * 사용자의 승인률 계산
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN pa.status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(pa) " +
           "FROM ParticipationApplication pa WHERE pa.user = :user")
    Double getApprovalRateByUser(@Param("user") User user);

    /**
     * 특정 게시글과 사용자로 참가 신청 조회
     */
    Optional<ParticipationApplication> findByTravelPostIdAndUserId(Long travelPostId, Long userId);

    /**
     * 특정 게시글의 특정 상태가 아닌 참가 신청 조회
     */
    List<ParticipationApplication> findByTravelPostIdAndStatusNot(Long travelPostId, String status);
} 