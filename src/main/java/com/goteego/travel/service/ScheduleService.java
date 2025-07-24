package com.goteego.travel.service;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.dto.ScheduleDto;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.travel.repository.ParticipationApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 일정 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final TravelPostRepository travelPostRepository;
    private final ParticipationApplicationRepository participationApplicationRepository;

    /**
     * 내 일정 조회 (페이징)
     */
    public ScheduleDto.MyScheduleResponse getMySchedules(Long userId, int page, int size) {
        log.info("내 일정 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        
        // 내가 작성한 게시글
        Page<TravelPost> myPosts = travelPostRepository.findByUserId(userId, pageable);
        
        // 내가 참여한 게시글
        Page<TravelPost> participatedPosts = travelPostRepository.findByParticipantUserId(userId, pageable);

        List<ScheduleDto.ScheduleItem> scheduleItems = myPosts.getContent().stream()
                .map(post -> convertToScheduleItem(post, userId, "OWNER", null))
                .collect(Collectors.toList());

        scheduleItems.addAll(participatedPosts.getContent().stream()
                .map(post -> {
                    String myStatus = participationApplicationRepository
                            .findByTravelPostIdAndUserId(post.getTravelPostId(), userId)
                            .map(app -> app.getStatus())
                            .orElse(null);
                    return convertToScheduleItem(post, userId, "PARTICIPANT", myStatus);
                })
                .collect(Collectors.toList()));

        return ScheduleDto.MyScheduleResponse.builder()
                .content(scheduleItems)
                .total_elements(myPosts.getTotalElements() + participatedPosts.getTotalElements())
                .build();
    }

    /**
     * 캘린더용 내 일정 조회 (전체)
     */
    public ScheduleDto.MyScheduleResponse getMySchedulesForCalendar(Long userId) {
        log.info("캘린더용 내 일정 조회: userId={}", userId);

        // 내가 작성한 게시글 (전체)
        List<TravelPost> myPosts = travelPostRepository.findAllByUserId(userId);
        
        // 내가 참여한 게시글 (전체)
        List<TravelPost> participatedPosts = travelPostRepository.findAllByParticipantUserId(userId);

        List<ScheduleDto.ScheduleItem> scheduleItems = myPosts.stream()
                .map(post -> convertToScheduleItem(post, userId, "OWNER", null))
                .collect(Collectors.toList());

        scheduleItems.addAll(participatedPosts.stream()
                .map(post -> {
                    String myStatus = participationApplicationRepository
                            .findByTravelPostIdAndUserId(post.getTravelPostId(), userId)
                            .map(app -> app.getStatus())
                            .orElse(null);
                    return convertToScheduleItem(post, userId, "PARTICIPANT", myStatus);
                })
                .collect(Collectors.toList()));

        return ScheduleDto.MyScheduleResponse.builder()
                .content(scheduleItems)
                .total_elements((long) scheduleItems.size())
                .build();
    }

    /**
     * TravelPost를 ScheduleItem으로 변환
     */
    private ScheduleDto.ScheduleItem convertToScheduleItem(TravelPost post, Long userId, String role, String myJoinStatus) {
        // 참가자 목록 조회 (REJECTED 제외)
        List<ScheduleDto.Participant> participants = participationApplicationRepository
                .findByTravelPostIdAndStatusNot(post.getTravelPostId(), "REJECTED")
                .stream()
                .map(app -> ScheduleDto.Participant.builder()
                        .user_id(app.getUserId())
                        .nickname("User " + app.getUserId()) // TODO: User 정보 조회
                        .status(app.getStatus())
                        .build())
                .collect(Collectors.toList());

        // 진행 상태 계산
        String progressStatus = calculateProgressStatus(post.getStartTime(), post.getEndTime());

        return ScheduleDto.ScheduleItem.builder()
                .travel_post_id(post.getTravelPostId())
                .title(post.getTitle())
                .start_time(post.getStartTime())
                .end_time(post.getEndTime())
                .is_add_recruit(post.getIsAddRecruit())
                .role(role)
                .my_join_status(myJoinStatus)
                .participants(participants)
                .progress_status(progressStatus)
                .build();
    }

    /**
     * 진행 상태 계산
     */
    private String calculateProgressStatus(LocalDate startTime, LocalDate endTime) {
        LocalDate now = LocalDate.now();
        
        if (now.isBefore(startTime)) {
            return "UPCOMING";
        } else if (now.isAfter(endTime)) {
            return "COMPLETED";
        } else {
            return "ONGOING";
        }
    }
} 