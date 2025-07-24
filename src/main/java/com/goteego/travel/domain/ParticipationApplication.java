package com.goteego.travel.domain;

import com.goteego.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 여행 동참 요청 엔티티
 * 사용자가 여행 게시글에 참가 신청한 정보를 저장
 */
@Entity
@Table(name = "participation_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ParticipationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_application_id")
    private Long participationApplicationId;

    // 여행 게시글 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_post_id", nullable = false)
    private TravelPost travelPost;

    // 신청자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 신청 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationStatus status = ParticipationStatus.PENDING;

    // 신청 시간
    @CreatedDate
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    /**
     * 참가 신청 상태 열거형
     */
    public enum ParticipationStatus {
        PENDING,   // 대기중
        APPROVED,  // 승인됨
        REJECTED   // 거절됨
    }

    /**
     * 신청 승인 처리
     */
    public void approve() {
        this.status = ParticipationStatus.APPROVED;
    }

    /**
     * 신청 거절 처리
     */
    public void reject() {
        this.status = ParticipationStatus.REJECTED;
    }

    /**
     * 대기 상태로 변경
     */
    public void setPending() {
        this.status = ParticipationStatus.PENDING;
    }

    /**
     * 승인된 신청인지 확인
     */
    public boolean isApproved() {
        return ParticipationStatus.APPROVED.equals(this.status);
    }

    /**
     * 거절된 신청인지 확인
     */
    public boolean isRejected() {
        return ParticipationStatus.REJECTED.equals(this.status);
    }

    /**
     * 대기 중인 신청인지 확인
     */
    public boolean isPending() {
        return ParticipationStatus.PENDING.equals(this.status);
    }

    /**
     * 처리 완료된 신청인지 확인 (승인 또는 거절)
     */
    public boolean isProcessed() {
        return isApproved() || isRejected();
    }

    // Controller에서 사용하는 메서드들
    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public Long getTravelPostId() {
        return travelPost != null ? travelPost.getTravelPostId() : null;
    }

    public String getStatus() {
        return status != null ? status.name() : null;
    }

    public String getMessage() {
        return "참가 신청"; // TODO: 실제 메시지 필드 추가
    }

    public LocalDateTime getCreatedAt() {
        return requestedAt;
    }

    public LocalDateTime getModifiedAt() {
        return requestedAt; // TODO: 실제 수정 시간 필드 추가
    }
} 