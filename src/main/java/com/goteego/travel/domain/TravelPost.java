package com.goteego.travel.domain;

import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 게시글 엔티티
 * 사전동행/현지동행 게시글 정보를 저장
 */
@Entity
@Table(name = "travel_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TravelPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_post_id")
    private Long travelPostId;

    // 작성자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 채팅방 정보 (게시글 생성 시 자동 생성)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // 게시글 기본 정보
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 여행 일정
    @Column(name = "start_time")
    private LocalDate startTime;

    @Column(name = "end_time")
    private LocalDate endTime;

    // 이미지 URL
    @Column(name = "image_url")
    private String imageUrl;

    // 모집 정보
    @Column(name = "recuit_limit")
    private Integer recuitLimit; // 모집 인원 제한

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L; // 조회수

    // 게시글 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType; // BEFORE(사전), NOW(현지)

    // 모집 상태
    @Column(name = "is_add_recruit")
    @Builder.Default
    private Boolean isAddRecruit = false; // false: 모집중, true: 모집완료

    // 생성/수정 시간
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    /**
     * 게시글 타입 열거형
     */
    public enum PostType {
        BEFORE, // 사전동행
        NOW     // 현지동행
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null) ? 1L : this.viewCount + 1L;
    }

    /**
     * 모집 완료 처리
     */
    public void completeRecruitment() {
        this.isAddRecruit = true;
    }

    /**
     * 모집 재개 처리
     */
    public void reopenRecruitment() {
        this.isAddRecruit = false;
    }

    /**
     * 여행 기간 계산 (일수)
     */
    public int getTravelDuration() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startTime, endTime) + 1;
    }

    /**
     * 여행 중인지 확인
     */
    public boolean isCurrentlyTraveling() {
        if (startTime == null || endTime == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isBefore(startTime) && !today.isAfter(endTime);
    }

    /**
     * 여행 예정인지 확인
     */
    public boolean isUpcomingTravel() {
        if (startTime == null) {
            return false;
        }
        return LocalDate.now().isBefore(startTime);
    }
} 