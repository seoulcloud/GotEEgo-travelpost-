package com.goteego.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 일정 관리 관련 DTO
 */
public class ScheduleDto {

    /**
     * 내 일정 조회 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyScheduleResponse {
        private List<ScheduleItem> content;
        private Long total_elements;
    }

    /**
     * 일정 아이템 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleItem {
        private Long travel_post_id;
        private String title;
        private LocalDate start_time;
        private LocalDate end_time;
        private Boolean is_add_recruit;
        private String role; // OWNER, PARTICIPANT
        private String my_join_status; // PENDING, APPROVED (PARTICIPANT일 때만)
        private List<Participant> participants;
        private String progress_status; // UPCOMING, ONGOING, COMPLETED
    }

    /**
     * 참가자 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participant {
        private Long user_id;
        private String nickname;
        private String status; // APPROVED, PENDING
    }
} 