package com.goteego.travel.dto;

import com.goteego.travel.domain.TravelPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 게시글 DTO
 */
public class TravelPostDto {

    /**
     * 게시글 생성 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long userId;
        private String title;
        private String content;
        private LocalDate startTime;
        private LocalDate endTime;
        private String imageUrl;
        private Integer recuitLimit;
        private TravelPost.PostType postType; // BEFORE(사전), NOW(현지)
    }

    /**
     * 게시글 수정 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String content;
        private LocalDate startTime;
        private LocalDate endTime;
        private String imageUrl;
        private Integer recuitLimit;
        private TravelPost.PostType postType;
    }

    /**
     * 게시글 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long travelPostId;
        private Long userId;
        private String userNickname;
        private String userProfileImgUrl;
        private Long chatRoomId;
        private String title;
        private String content;
        private LocalDate startTime;
        private LocalDate endTime;
        private String imageUrl;
        private Integer recuitLimit;
        private Long viewCount;
        private TravelPost.PostType postType;
        private Boolean isAddRecruit;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
        private Integer travelDuration; // 여행 기간 (일수)
        private Boolean isCurrentlyTraveling; // 현재 여행 중인지
        private Boolean isUpcomingTravel; // 예정된 여행인지
    }

    /**
     * 게시글 목록 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long travelPostId;
        private Long userId;
        private String userNickname;
        private String userProfileImgUrl;
        private String title;
        private String content;
        private LocalDate startTime;
        private LocalDate endTime;
        private String imageUrl;
        private Integer recuitLimit;
        private Long viewCount;
        private TravelPost.PostType postType;
        private Boolean isAddRecruit;
        private LocalDateTime createdAt;
        private Integer travelDuration;
        private Boolean isCurrentlyTraveling;
        private Boolean isUpcomingTravel;
    }

    /**
     * 게시글 검색 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String keyword; // 제목 또는 내용 검색
        private TravelPost.PostType postType; // 게시글 타입 필터
        private Boolean isAddRecruit; // 모집 상태 필터
        private LocalDate startDate; // 시작일 범위
        private LocalDate endDate; // 종료일 범위
        private String sortBy; // 정렬 기준 (viewCount, createdAt, startTime)
        private String sortOrder; // 정렬 순서 (asc, desc)
        private Integer page; // 페이지 번호
        private Integer size; // 페이지 크기
    }

    /**
     * 게시글 통계 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsResponse {
        private Long totalPosts;
        private Long beforePosts; // 사전동행 게시글 수
        private Long nowPosts; // 현지동행 게시글 수
        private Long recruitingPosts; // 모집 중인 게시글 수
        private Long completedPosts; // 모집 완료된 게시글 수
        private Double averageViewCount;
        private Long currentlyTravelingPosts; // 현재 여행 중인 게시글 수
        private Long upcomingTravelPosts; // 예정된 여행 게시글 수
    }
} 