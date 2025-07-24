/*
package com.goteego.travel.controller;

import com.goteego.travel.domain.ParticipationApplication;
import com.goteego.travel.dto.ParticipationApplicationDto;
import com.goteego.travel.service.ParticipationApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.stream.Collectors;

/**
 * 참가 신청 관리 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/participation-applications")
@RequiredArgsConstructor
public class ParticipationApplicationController {

    private final ParticipationApplicationService participationApplicationService;

    /**
     * 참가 신청 생성
     * POST /api/participation-applications
     */
    @PostMapping
    public ResponseEntity<ParticipationApplicationDto.Response> createParticipationApplication(
            @RequestBody ParticipationApplicationDto.CreateRequest request) {
        log.info("참가 신청 생성 요청: userId={}, travelPostId={}", request.getUserId(), request.getTravelPostId());

        try {
            // 서비스 메서드를 통해 참가 신청 생성
            ParticipationApplication savedApplication = participationApplicationService.createApplication(
                    request.getTravelPostId(), 
                    null // TODO: User 객체 조회 로직 필요
            );
            ParticipationApplicationDto.Response response = convertToResponse(savedApplication);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("참가 신청 생성 실패 (이미 신청됨): userId={}, travelPostId={}, error={}", 
                    request.getUserId(), request.getTravelPostId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("참가 신청 생성 실패: userId={}, travelPostId={}, error={}", 
                    request.getUserId(), request.getTravelPostId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 참가 신청 조회
     * GET /api/participation-applications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParticipationApplicationDto.Response> getParticipationApplication(@PathVariable Long id) {
        log.info("참가 신청 조회 요청: id={}", id);

        try {
            ParticipationApplication application = participationApplicationService.getParticipationApplication(id);
            ParticipationApplicationDto.Response response = convertToResponse(application);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("참가 신청 조회 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자의 참가 신청 목록 조회
     * GET /api/participation-applications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ParticipationApplicationDto.Response>> getUserParticipationApplications(
            @PathVariable Long userId,
            @RequestParam(required = false) String status) {
        log.info("사용자 참가 신청 목록 조회 요청: userId={}, status={}", userId, status);

        try {
            List<ParticipationApplication> applications;
            if (status != null) {
                applications = participationApplicationService.getUserParticipationApplicationsByStatus(userId, status);
            } else {
                applications = participationApplicationService.getUserParticipationApplications(userId);
            }

            List<ParticipationApplicationDto.Response> responses = applications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("사용자 참가 신청 목록 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 여행 게시글의 참가 신청 목록 조회
     * GET /api/participation-applications/travel-post/{travelPostId}
     */
    @GetMapping("/travel-post/{travelPostId}")
    public ResponseEntity<List<ParticipationApplicationDto.Response>> getTravelPostParticipationApplications(
            @PathVariable Long travelPostId,
            @RequestParam(required = false) String status) {
        log.info("여행 게시글 참가 신청 목록 조회 요청: travelPostId={}, status={}", travelPostId, status);

        try {
            List<ParticipationApplication> applications;
            if (status != null) {
                applications = participationApplicationService.getTravelPostParticipationApplicationsByStatus(travelPostId, status);
            } else {
                applications = participationApplicationService.getTravelPostParticipationApplications(travelPostId);
            }

            List<ParticipationApplicationDto.Response> responses = applications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("여행 게시글 참가 신청 목록 조회 실패: travelPostId={}, error={}", travelPostId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 참가자 상태 업데이트 (새로운 API)
     * PUT /api/schedule/{travel_post_id}/participants/{user_id}
     */
    @PutMapping("/schedule/{travel_post_id}/participants/{user_id}")
    public ResponseEntity<ParticipationApplicationDto.StatusUpdateResponse> updateParticipantStatus(
            @PathVariable Long travel_post_id,
            @PathVariable Long user_id,
            @RequestBody ParticipationApplicationDto.StatusUpdateRequest request) {
        log.info("참가자 상태 업데이트 요청: travel_post_id={}, user_id={}, status={}", 
                travel_post_id, user_id, request.getStatus());

        try {
            // TODO: 인증/권한 확인 로직 추가
            // Long currentUserId = getCurrentUserId();
            // if (!isTravelPostOwner(travel_post_id, currentUserId)) {
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            // }

            participationApplicationService.updateParticipantStatus(
                    travel_post_id, user_id, request.getStatus());
            
            ParticipationApplicationDto.StatusUpdateResponse response = ParticipationApplicationDto.StatusUpdateResponse.builder()
                    .message("Participant status updated successfully.")
                    .travel_post_id(travel_post_id)
                    .participant_user_id(user_id)
                    .new_status(request.getStatus())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("참가자 상태 업데이트 실패 (참가 신청 없음): travel_post_id={}, user_id={}, error={}", 
                    travel_post_id, user_id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("참가자 상태 업데이트 실패: travel_post_id={}, user_id={}, error={}", 
                    travel_post_id, user_id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 참가 신청 승인 (기존 API - 호환성)
     * POST /api/participation-applications/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ParticipationApplicationDto.Response> approveParticipationApplication(@PathVariable Long id) {
        log.info("참가 신청 승인 요청: id={}", id);

        try {
            ParticipationApplication approvedApplication = participationApplicationService.approveParticipationApplication(id);
            ParticipationApplicationDto.Response response = convertToResponse(approvedApplication);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("참가 신청 승인 실패 (신청 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("참가 신청 승인 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 참가 신청 거절
     * POST /api/participation-applications/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ParticipationApplicationDto.Response> rejectParticipationApplication(@PathVariable Long id) {
        log.info("참가 신청 거절 요청: id={}", id);

        try {
            ParticipationApplication rejectedApplication = participationApplicationService.rejectParticipationApplication(id);
            ParticipationApplicationDto.Response response = convertToResponse(rejectedApplication);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("참가 신청 거절 실패 (신청 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("참가 신청 거절 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 참가 신청 취소
     * POST /api/participation-applications/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ParticipationApplicationDto.Response> cancelParticipationApplication(@PathVariable Long id) {
        log.info("참가 신청 취소 요청: id={}", id);

        try {
            ParticipationApplication cancelledApplication = participationApplicationService.cancelParticipationApplication(id);
            ParticipationApplicationDto.Response response = convertToResponse(cancelledApplication);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("참가 신청 취소 실패 (신청 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("참가 신청 취소 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 참가 신청 삭제
     * DELETE /api/participation-applications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipationApplication(@PathVariable Long id) {
        log.info("참가 신청 삭제 요청: id={}", id);

        try {
            participationApplicationService.deleteParticipationApplication(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("참가 신청 삭제 실패 (신청 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("참가 신청 삭제 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 참가 신청 통계 조회
     * GET /api/participation-applications/statistics?travelPostId=1
     */
    @GetMapping("/statistics")
    public ResponseEntity<ParticipationApplicationDto.StatisticsResponse> getParticipationStatistics(
            @RequestParam(required = false) Long travelPostId,
            @RequestParam(required = false) Long userId) {
        log.info("참가 신청 통계 조회 요청: travelPostId={}, userId={}", travelPostId, userId);

        try {
            ParticipationApplicationDto.StatisticsResponse statistics;
            if (travelPostId != null) {
                statistics = participationApplicationService.getTravelPostParticipationStatistics(travelPostId);
            } else if (userId != null) {
                statistics = participationApplicationService.getUserParticipationStatistics(userId);
            } else {
                statistics = participationApplicationService.getOverallParticipationStatistics();
            }

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("참가 신청 통계 조회 실패: travelPostId={}, userId={}, error={}", travelPostId, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 대기 중인 참가 신청 목록 조회
     * GET /api/participation-applications/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ParticipationApplicationDto.Response>> getPendingApplications() {
        log.info("대기 중인 참가 신청 목록 조회 요청");

        try {
            List<ParticipationApplication> pendingApplications = participationApplicationService.getPendingApplications();
            List<ParticipationApplicationDto.Response> responses = pendingApplications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("대기 중인 참가 신청 목록 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 승인된 참가 신청 목록 조회
     * GET /api/participation-applications/approved
     */
    @GetMapping("/approved")
    public ResponseEntity<List<ParticipationApplicationDto.Response>> getApprovedApplications() {
        log.info("승인된 참가 신청 목록 조회 요청");

        try {
            List<ParticipationApplication> approvedApplications = participationApplicationService.getApprovedApplications();
            List<ParticipationApplicationDto.Response> responses = approvedApplications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("승인된 참가 신청 목록 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTO 변환 메서드
    private ParticipationApplicationDto.Response convertToResponse(ParticipationApplication application) {
        return ParticipationApplicationDto.Response.builder()
                .participationApplicationId(application.getParticipationApplicationId())
                .userId(application.getUserId())
                .travelPostId(application.getTravelPostId())
                .status(application.getStatus())
                .message(application.getMessage())
                .createdAt(application.getCreatedAt())
                .modifiedAt(application.getModifiedAt())
                .build();
    }
}
*/