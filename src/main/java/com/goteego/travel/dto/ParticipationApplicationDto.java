package com.goteego.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 참가 신청 관련 DTO
 */
public class ParticipationApplicationDto {

    /**
     * 참가 신청 생성 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long userId;
        private Long travelPostId;
        private String message;
    }

    /**
     * 참가 신청 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long participationApplicationId;
        private Long userId;
        private Long travelPostId;
        private String status; // PENDING, APPROVED, REJECTED, CANCELLED
        private String message;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

    /**
     * 참가 신청 통계 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsResponse {
        private Long totalApplications;
        private Long pendingApplications;
        private Long approvedApplications;
        private Long rejectedApplications;
        private Long cancelledApplications;
        private Double approvalRate; // 승인률 (0.0 ~ 1.0)
    }

    /**
     * 참가자 상태 업데이트 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        private String status; // APPROVED, REJECTED
    }

    /**
     * 참가자 상태 업데이트 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateResponse {
        private String message;
        private Long travel_post_id;
        private Long participant_user_id;
        private String new_status;
    }
} 