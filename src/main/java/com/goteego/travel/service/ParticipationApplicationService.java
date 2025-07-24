package com.goteego.travel.service;

import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.dto.ParticipationApplicationDto;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 참가 신청 서비스
 * 신청, 승인, 거절, 알림 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationApplicationService {

    private final ParticipationApplicationRepository participationApplicationRepository;
    private final TravelPostService travelPostService;

    /**
     * 참가 신청 생성
     */
    @Transactional
    public ParticipationApplication createApplication(Long travelPostId, User user) {
        log.info("참가 신청 생성: travelPostId={}, userId={}", travelPostId, user.getUserId());
        
        // 게시글 존재 확인
        Optional<TravelPost> travelPostOpt = travelPostService.getTravelPost(travelPostId);
        if (travelPostOpt.isEmpty()) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: travelPostId=" + travelPostId);
        }

        TravelPost travelPost = travelPostOpt.get();
        
        // 이미 신청했는지 확인
        Optional<ParticipationApplication> existingApplication = 
                participationApplicationRepository.findByUserAndTravelPost(user, travelPost);
        if (existingApplication.isPresent()) {
            throw new IllegalStateException("이미 참가 신청을 했습니다: travelPostId=" + travelPostId);
        }
        
        // 게시글 작성자 본인은 신청 불가
        if (travelPost.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("자신이 작성한 게시글에는 참가 신청할 수 없습니다");
        }
        
        // 모집이 완료된 게시글에는 신청 불가
        if (travelPost.getIsAddRecruit()) {
            throw new IllegalStateException("모집이 완료된 게시글입니다: travelPostId=" + travelPostId);
        }

        ParticipationApplication application = ParticipationApplication.builder()
                .travelPost(travelPost)
                .user(user)
                .status(ParticipationApplication.ParticipationStatus.PENDING)
                .build();

        ParticipationApplication savedApplication = participationApplicationRepository.save(application);
        
        // TODO: 게시글 작성자에게 알림 발송
        // sendNotificationToPostAuthor(travelPost, user);
        
        return savedApplication;
    }

    /**
     * 참가 신청 승인
     */
    @Transactional
    public ParticipationApplication approveApplication(Long applicationId) {
        log.info("참가 신청 승인: applicationId={}", applicationId);
        
        Optional<ParticipationApplication> applicationOpt = 
                participationApplicationRepository.findByParticipationApplicationId(applicationId);
        if (applicationOpt.isEmpty()) {
            throw new IllegalArgumentException("참가 신청을 찾을 수 없습니다: applicationId=" + applicationId);
        }

        ParticipationApplication application = applicationOpt.get();
        
        // 이미 처리된 신청인지 확인
        if (application.isProcessed()) {
            throw new IllegalStateException("이미 처리된 참가 신청입니다: applicationId=" + applicationId);
        }

        application.approve();
        ParticipationApplication savedApplication = participationApplicationRepository.save(application);
        
        // TODO: 승인된 사용자를 채팅방에 초대
        // inviteUserToChatRoom(application);
        
        // TODO: 승인된 사용자에게 알림 발송
        // sendApprovalNotification(application);
        
        return savedApplication;
    }

    /**
     * 참가 신청 거절
     */
    @Transactional
    public ParticipationApplication rejectApplication(Long applicationId) {
        log.info("참가 신청 거절: applicationId={}", applicationId);
        
        Optional<ParticipationApplication> applicationOpt = 
                participationApplicationRepository.findByParticipationApplicationId(applicationId);
        if (applicationOpt.isEmpty()) {
            throw new IllegalArgumentException("참가 신청을 찾을 수 없습니다: applicationId=" + applicationId);
        }

        ParticipationApplication application = applicationOpt.get();
        
        // 이미 처리된 신청인지 확인
        if (application.isProcessed()) {
            throw new IllegalStateException("이미 처리된 참가 신청입니다: applicationId=" + applicationId);
        }

        application.reject();
        ParticipationApplication savedApplication = participationApplicationRepository.save(application);
        
        // TODO: 거절된 사용자에게 알림 발송
        // sendRejectionNotification(application);
        
        return savedApplication;
    }

    /**
     * 참가 신청 조회
     */
    public Optional<ParticipationApplication> getApplication(Long applicationId) {
        log.info("참가 신청 조회: applicationId={}", applicationId);
        return participationApplicationRepository.findByParticipationApplicationId(applicationId);
    }

    /**
     * 참가 신청 조회 (Controller용)
     */
    public ParticipationApplication getParticipationApplication(Long id) {
        log.info("참가 신청 조회: id={}", id);
        return participationApplicationRepository.findByParticipationApplicationId(id)
                .orElseThrow(() -> new IllegalArgumentException("참가 신청을 찾을 수 없습니다: id=" + id));
    }

    /**
     * 사용자별 참가 신청 조회
     */
    public List<ParticipationApplication> getApplicationsByUser(User user) {
        log.info("사용자별 참가 신청 조회: userId={}", user.getUserId());
        return participationApplicationRepository.findByUserOrderByRequestedAtDesc(user);
    }

    /**
     * 게시글별 참가 신청 조회
     */
    public List<ParticipationApplication> getApplicationsByTravelPost(TravelPost travelPost) {
        log.info("게시글별 참가 신청 조회: travelPostId={}", travelPost.getTravelPostId());
        return participationApplicationRepository.findByTravelPostOrderByRequestedAtAsc(travelPost);
    }

    /**
     * 상태별 참가 신청 조회
     */
    public List<ParticipationApplication> getApplicationsByStatus(ParticipationApplication.ParticipationStatus status) {
        log.info("상태별 참가 신청 조회: status={}", status);
        return participationApplicationRepository.findByStatus(status);
    }

    /**
     * 사용자의 승인된 참가 신청 조회
     */
    public List<ParticipationApplication> getApprovedApplicationsByUser(User user) {
        log.info("사용자의 승인된 참가 신청 조회: userId={}", user.getUserId());
        return participationApplicationRepository.findApprovedApplicationsByUser(user);
    }

    /**
     * 사용자의 대기 중인 참가 신청 조회
     */
    public List<ParticipationApplication> getPendingApplicationsByUser(User user) {
        log.info("사용자의 대기 중인 참가 신청 조회: userId={}", user.getUserId());
        return participationApplicationRepository.findPendingApplicationsByUser(user);
    }

    /**
     * 사용자의 거절된 참가 신청 조회
     */
    public List<ParticipationApplication> getRejectedApplicationsByUser(User user) {
        log.info("사용자의 거절된 참가 신청 조회: userId={}", user.getUserId());
        return participationApplicationRepository.findRejectedApplicationsByUser(user);
    }

    /**
     * 게시글 작성자가 받은 참가 신청 조회
     */
    public List<ParticipationApplication> getApplicationsForPostAuthor(User postAuthor) {
        log.info("게시글 작성자가 받은 참가 신청 조회: userId={}", postAuthor.getUserId());
        return participationApplicationRepository.findApplicationsForPostAuthor(postAuthor);
    }

    /**
     * 게시글 작성자가 받은 대기 중인 참가 신청 조회
     */
    public List<ParticipationApplication> getPendingApplicationsForPostAuthor(User postAuthor) {
        log.info("게시글 작성자가 받은 대기 중인 참가 신청 조회: userId={}", postAuthor.getUserId());
        return participationApplicationRepository.findPendingApplicationsForPostAuthor(postAuthor);
    }

    /**
     * 특정 사용자가 특정 게시글에 신청했는지 확인
     */
    public boolean hasUserApplied(Long travelPostId, User user) {
        Optional<TravelPost> travelPostOpt = travelPostService.getTravelPost(travelPostId);
        if (travelPostOpt.isEmpty()) {
            return false;
        }
        
        Optional<ParticipationApplication> applicationOpt = 
                participationApplicationRepository.findByUserAndTravelPost(user, travelPostOpt.get());
        return applicationOpt.isPresent();
    }

    /**
     * 특정 게시글의 승인된 참가자 수 조회
     */
    public long countApprovedApplicationsByTravelPost(TravelPost travelPost) {
        return participationApplicationRepository.countApprovedApplicationsByTravelPost(travelPost);
    }

    /**
     * 특정 게시글의 대기 중인 신청 수 조회
     */
    public long countPendingApplicationsByTravelPost(TravelPost travelPost) {
        return participationApplicationRepository.countPendingApplicationsByTravelPost(travelPost);
    }

    /**
     * 사용자가 승인된 여행 게시글 조회
     */
    public List<TravelPost> getApprovedTravelPostsByUser(User user) {
        log.info("사용자가 승인된 여행 게시글 조회: userId={}", user.getUserId());
        return participationApplicationRepository.findApprovedTravelPostsByUser(user);
    }

    /**
     * 참가 신청 삭제
     */
    @Transactional
    public void deleteApplication(Long applicationId) {
        log.info("참가 신청 삭제: applicationId={}", applicationId);
        
        if (!participationApplicationRepository.existsById(applicationId)) {
            throw new IllegalArgumentException("참가 신청을 찾을 수 없습니다: applicationId=" + applicationId);
        }
        
        participationApplicationRepository.deleteById(applicationId);
    }

    /**
     * 통계 조회
     */
    public long countByStatus(ParticipationApplication.ParticipationStatus status) {
        return participationApplicationRepository.countByStatus(status);
    }

    public long countByUser(User user) {
        return participationApplicationRepository.countByUser(user);
    }

    public long countByTravelPost(TravelPost travelPost) {
        return participationApplicationRepository.countByTravelPost(travelPost);
    }

    /**
     * 사용자의 승인률 계산
     */
    public Double getApprovalRateByUser(User user) {
        return participationApplicationRepository.getApprovalRateByUser(user);
    }

    /**
     * 참가 신청 존재 여부 확인
     */
    public boolean existsByApplicationId(Long applicationId) {
        return participationApplicationRepository.existsById(applicationId);
    }

    // Controller에서 사용하는 메서드들 추가
    /**
     * 참가 신청 생성 (Entity 기반)
     */
    @Transactional
    public ParticipationApplication createParticipationApplication(ParticipationApplication application) {
        log.info("참가 신청 생성: travelPostId={}, userId={}", 
                application.getTravelPostId(), application.getUserId());
        return participationApplicationRepository.save(application);
    }

    /**
     * 참가 신청 승인 (기존 메서드)
     */
    @Transactional
    public ParticipationApplication approveParticipationApplication(Long id) {
        return approveApplication(id);
    }

    /**
     * 참가 신청 거절 (기존 메서드)
     */
    @Transactional
    public ParticipationApplication rejectParticipationApplication(Long id) {
        return rejectApplication(id);
    }

    /**
     * 참가 신청 취소
     */
    @Transactional
    public ParticipationApplication cancelParticipationApplication(Long id) {
        log.info("참가 신청 취소: id={}", id);
        
        Optional<ParticipationApplication> applicationOpt = 
                participationApplicationRepository.findByParticipationApplicationId(id);
        if (applicationOpt.isEmpty()) {
            throw new IllegalArgumentException("참가 신청을 찾을 수 없습니다: id=" + id);
        }

        ParticipationApplication application = applicationOpt.get();
        application.setPending(); // 대기 상태로 변경
        return participationApplicationRepository.save(application);
    }

    /**
     * 참가 신청 삭제 (기존 메서드)
     */
    @Transactional
    public void deleteParticipationApplication(Long id) {
        deleteApplication(id);
    }

    /**
     * 사용자별 참가 신청 조회
     */
    public List<ParticipationApplication> getUserParticipationApplications(Long userId) {
        // TODO: User 객체 조회 로직 필요
        log.info("사용자별 참가 신청 조회: userId={}", userId);
        return List.of(); // 임시 반환
    }

    /**
     * 사용자별 상태별 참가 신청 조회
     */
    public List<ParticipationApplication> getUserParticipationApplicationsByStatus(Long userId, String status) {
        log.info("사용자별 상태별 참가 신청 조회: userId={}, status={}", userId, status);
        return List.of(); // 임시 반환
    }

    /**
     * 게시글별 참가 신청 조회
     */
    public List<ParticipationApplication> getTravelPostParticipationApplications(Long travelPostId) {
        log.info("게시글별 참가 신청 조회: travelPostId={}", travelPostId);
        return List.of(); // 임시 반환
    }

    /**
     * 게시글별 상태별 참가 신청 조회
     */
    public List<ParticipationApplication> getTravelPostParticipationApplicationsByStatus(Long travelPostId, String status) {
        log.info("게시글별 상태별 참가 신청 조회: travelPostId={}, status={}", travelPostId, status);
        return List.of(); // 임시 반환
    }

    /**
     * 대기 중인 참가 신청 조회
     */
    public List<ParticipationApplication> getPendingApplications() {
        log.info("대기 중인 참가 신청 조회");
        return getApplicationsByStatus(ParticipationApplication.ParticipationStatus.PENDING);
    }

    /**
     * 승인된 참가 신청 조회
     */
    public List<ParticipationApplication> getApprovedApplications() {
        log.info("승인된 참가 신청 조회");
        return getApplicationsByStatus(ParticipationApplication.ParticipationStatus.APPROVED);
    }

    /**
     * 통계 조회 메서드들
     */
    public ParticipationApplicationDto.StatisticsResponse getTravelPostParticipationStatistics(Long travelPostId) {
        log.info("게시글별 참가 신청 통계 조회: travelPostId={}", travelPostId);
        return new ParticipationApplicationDto.StatisticsResponse(); // 임시 반환
    }

    public ParticipationApplicationDto.StatisticsResponse getUserParticipationStatistics(Long userId) {
        log.info("사용자별 참가 신청 통계 조회: userId={}", userId);
        return new ParticipationApplicationDto.StatisticsResponse(); // 임시 반환
    }

    public ParticipationApplicationDto.StatisticsResponse getOverallParticipationStatistics() {
        log.info("전체 참가 신청 통계 조회");
        return new ParticipationApplicationDto.StatisticsResponse(); // 임시 반환
    }

    /**
     * 참가자 상태 업데이트 (새로운 API용)
     */
    @Transactional
    public ParticipationApplication updateParticipantStatus(Long travelPostId, Long userId, String status) {
        log.info("참가자 상태 업데이트: travelPostId={}, userId={}, status={}", travelPostId, userId, status);
        
        // 상태 값 검증
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("허용되지 않는 상태 값입니다: " + status);
        }
        
        // TODO: 실제 구현 필요
        // 1. travelPostId와 userId로 참가 신청 조회
        // 2. 상태 업데이트
        // 3. 저장 및 반환
        
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }
} 