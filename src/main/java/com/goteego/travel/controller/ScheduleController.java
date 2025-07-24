/*
package com.goteego.travel.controller;

import com.goteego.travel.dto.ScheduleDto;
import com.goteego.travel.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



/**
 * 일정 관리 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 내 일정 조회
     * GET /api/schedule/mine?page=0&size=10
     */
    @GetMapping("/mine")
    public ResponseEntity<ScheduleDto.MyScheduleResponse> getMySchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("내 일정 조회 요청: page={}, size={}", page, size);

        try {
            // TODO: 인증된 사용자 ID 가져오기
            // Long currentUserId = getCurrentUserId();
            Long currentUserId = 1L; // 임시 값

            ScheduleDto.MyScheduleResponse response = scheduleService.getMySchedules(currentUserId, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("내 일정 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 캘린더용 일정 조회 (페이징 없이 전체)
     * GET /api/schedule/mine/calendar
     */
    @GetMapping("/mine/calendar")
    public ResponseEntity<ScheduleDto.MyScheduleResponse> getMySchedulesForCalendar() {
        log.info("캘린더용 내 일정 조회 요청");

        try {
            // TODO: 인증된 사용자 ID 가져오기
            Long currentUserId = 1L; // 임시 값

            ScheduleDto.MyScheduleResponse response = scheduleService.getMySchedulesForCalendar(currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("캘린더용 내 일정 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
*/