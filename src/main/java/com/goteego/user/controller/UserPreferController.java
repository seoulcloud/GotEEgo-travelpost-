package com.goteego.user.controller;

import com.goteego.user.domain.UserPrefer;
import com.goteego.user.dto.UserPreferDto;
import com.goteego.user.service.UserPreferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 선호도 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/user-prefer")
@RequiredArgsConstructor
public class UserPreferController {

    private final UserPreferService userPreferService;

    /**
     * 사용자 선호도 저장/수정
     * POST /api/user-prefer
     */
    @PostMapping
    public ResponseEntity<UserPreferDto.Response> saveUserPrefer(
            @RequestBody UserPreferDto.CreateRequest request) {
        log.info("사용자 선호도 저장/수정 요청: userId={}", request.getUserId());

        try {
            UserPrefer userPrefer = UserPrefer.builder()
                    .userId(request.getUserId())
                    .isAlchol3(request.getIsAlchol3())
                    .isAlchol2(request.getIsAlchol2())
                    .isAlchol1(request.getIsAlchol1())
                    .isSmoker(request.getIsSmoker())
                    .isFriendly(request.getIsFriendly())
                    .isQuiet(request.getIsQuiet())
                    .isLead(request.getIsLead())
                    .isParty(request.getIsParty())
                    .isSearch(request.getIsSearch())
                    .isListen(request.getIsListen())
                    .isSee(request.getIsSee())
                    .isCafe(request.getIsCafe())
                    .isTaste(request.getIsTaste())
                    .isPicture(request.getIsPicture())
                    .isShopping(request.getIsShopping())
                    .isOutdoor(request.getIsOutdoor())
                    .isChill(request.getIsChill())
                    .isBusy(request.getIsBusy())
                    .isFlex(request.getIsFlex())
                    .isCity(request.getIsCity())
                    .isHeal(request.getIsHeal())
                    .isBeach(request.getIsBeach())
                    .isMountain(request.getIsMountain())
                    .build();

            UserPrefer savedPrefer = userPreferService.saveUserPrefer(userPrefer);
            UserPreferDto.Response response = convertToResponse(savedPrefer);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 선호도 저장/수정 실패: userId={}, error={}", request.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자 선호도 조회
     * GET /api/user-prefer/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserPreferDto.Response> getUserPrefer(@PathVariable Long userId) {
        log.info("사용자 선호도 조회 요청: userId={}", userId);

        try {
            Optional<UserPrefer> userPreferOpt = userPreferService.getUserPreferByUserId(userId);
            if (userPreferOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserPreferDto.Response response = convertToResponse(userPreferOpt.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 선호도 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자 선호도 삭제
     * DELETE /api/user-prefer/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserPrefer(@PathVariable Long userId) {
        log.info("사용자 선호도 삭제 요청: userId={}", userId);

        try {
            if (!userPreferService.existsByUserId(userId)) {
                return ResponseEntity.notFound().build();
            }

            userPreferService.deleteUserPrefer(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("사용자 선호도 삭제 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 유사한 선호도를 가진 사용자들 조회
     * GET /api/user-prefer/similar/{userId}
     */
    @GetMapping("/similar/{userId}")
    public ResponseEntity<List<UserPreferDto.SimilarUserResponse>> getSimilarUsers(@PathVariable Long userId) {
        log.info("유사한 선호도를 가진 사용자들 조회 요청: userId={}", userId);

        try {
            Optional<UserPrefer> targetUserPreferOpt = userPreferService.getUserPreferByUserId(userId);
            if (targetUserPreferOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserPrefer targetUserPrefer = targetUserPreferOpt.get();
            List<UserPrefer> similarUsers = userPreferService.getSimilarUsers(targetUserPrefer);

            List<UserPreferDto.SimilarUserResponse> responses = similarUsers.stream()
                    .limit(10) // 상위 10명만 반환
                    .map(this::convertToSimilarUserResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("유사한 선호도를 가진 사용자들 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 친화적인 사용자들 조회
     * GET /api/user-prefer/friendly
     */
    @GetMapping("/friendly")
    public ResponseEntity<List<UserPreferDto.Response>> getFriendlyUsers() {
        log.info("친화적인 사용자들 조회 요청");

        try {
            List<UserPrefer> friendlyUsers = userPreferService.getFriendlyUsers();
            List<UserPreferDto.Response> responses = friendlyUsers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("친화적인 사용자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 리더십 있는 사용자들 조회
     * GET /api/user-prefer/lead
     */
    @GetMapping("/lead")
    public ResponseEntity<List<UserPreferDto.Response>> getLeadUsers() {
        log.info("리더십 있는 사용자들 조회 요청");

        try {
            List<UserPrefer> leadUsers = userPreferService.getLeadUsers();
            List<UserPreferDto.Response> responses = leadUsers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("리더십 있는 사용자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 아웃도어 활동을 좋아하는 사용자들 조회
     * GET /api/user-prefer/outdoor
     */
    @GetMapping("/outdoor")
    public ResponseEntity<List<UserPreferDto.Response>> getOutdoorUsers() {
        log.info("아웃도어 활동을 좋아하는 사용자들 조회 요청");

        try {
            List<UserPrefer> outdoorUsers = userPreferService.getOutdoorUsers();
            List<UserPreferDto.Response> responses = outdoorUsers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("아웃도어 활동을 좋아하는 사용자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 도시 여행을 선호하는 사용자들 조회
     * GET /api/user-prefer/city
     */
    @GetMapping("/city")
    public ResponseEntity<List<UserPreferDto.Response>> getCityUsers() {
        log.info("도시 여행을 선호하는 사용자들 조회 요청");

        try {
            List<UserPrefer> cityUsers = userPreferService.getCityUsers();
            List<UserPreferDto.Response> responses = cityUsers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("도시 여행을 선호하는 사용자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 힐링 여행을 선호하는 사용자들 조회
     * GET /api/user-prefer/heal
     */
    @GetMapping("/heal")
    public ResponseEntity<List<UserPreferDto.Response>> getHealUsers() {
        log.info("힐링 여행을 선호하는 사용자들 조회 요청");

        try {
            List<UserPrefer> healUsers = userPreferService.getHealUsers();
            List<UserPreferDto.Response> responses = healUsers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("힐링 여행을 선호하는 사용자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 술을 좋아하는 사용자들 조회
     * GET /api/user-prefer/alcohol-lovers
     */
    @GetMapping("/alcohol-lovers")
    public ResponseEntity<List<UserPreferDto.Response>> getAlcoholLovers() {
        log.info("술을 좋아하는 사용자들 조회 요청");

        try {
            List<UserPrefer> alcoholLovers = userPreferService.getAlcoholLovers();
            List<UserPreferDto.Response> responses = alcoholLovers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("술을 좋아하는 사용자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 술을 마시지 않는 사용자들 조회
     * GET /api/user-prefer/non-alcohol
     */
    @GetMapping("/non-alcohol")
    public ResponseEntity<List<UserPreferDto.Response>> getNonAlcoholUsers() {
        log.info("술을 마시지 않는 사용자들 조회 요청");

        try {
            List<UserPrefer> nonAlcoholUsers = userPreferService.getNonAlcoholUsers();
            List<UserPreferDto.Response> responses = nonAlcoholUsers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("술을 마시지 않는 사용자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 흡연자들 조회
     * GET /api/user-prefer/smokers
     */
    @GetMapping("/smokers")
    public ResponseEntity<List<UserPreferDto.Response>> getSmokers() {
        log.info("흡연자들 조회 요청");

        try {
            List<UserPrefer> smokers = userPreferService.getSmokers();
            List<UserPreferDto.Response> responses = smokers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("흡연자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 비흡연자들 조회
     * GET /api/user-prefer/non-smokers
     */
    @GetMapping("/non-smokers")
    public ResponseEntity<List<UserPreferDto.Response>> getNonSmokers() {
        log.info("비흡연자들 조회 요청");

        try {
            List<UserPrefer> nonSmokers = userPreferService.getNonSmokers();
            List<UserPreferDto.Response> responses = nonSmokers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("비흡연자들 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTO 변환 메서드들
    private UserPreferDto.Response convertToResponse(UserPrefer userPrefer) {
        return UserPreferDto.Response.builder()
                .userId(userPrefer.getUserId())
                .isAlchol3(userPrefer.getIsAlchol3())
                .isAlchol2(userPrefer.getIsAlchol2())
                .isAlchol1(userPrefer.getIsAlchol1())
                .isSmoker(userPrefer.getIsSmoker())
                .isFriendly(userPrefer.getIsFriendly())
                .isQuiet(userPrefer.getIsQuiet())
                .isLead(userPrefer.getIsLead())
                .isParty(userPrefer.getIsParty())
                .isSearch(userPrefer.getIsSearch())
                .isListen(userPrefer.getIsListen())
                .isSee(userPrefer.getIsSee())
                .isCafe(userPrefer.getIsCafe())
                .isTaste(userPrefer.getIsTaste())
                .isPicture(userPrefer.getIsPicture())
                .isShopping(userPrefer.getIsShopping())
                .isOutdoor(userPrefer.getIsOutdoor())
                .isChill(userPrefer.getIsChill())
                .isBusy(userPrefer.getIsBusy())
                .isFlex(userPrefer.getIsFlex())
                .isCity(userPrefer.getIsCity())
                .isHeal(userPrefer.getIsHeal())
                .isBeach(userPrefer.getIsBeach())
                .isMountain(userPrefer.getIsMountain())
                .createdAt(userPrefer.getCreatedAt())
                .modifiedAt(userPrefer.getModifiedAt())
                .build();
    }

    private UserPreferDto.SimilarUserResponse convertToSimilarUserResponse(UserPrefer userPrefer) {
        // TODO: User 정보 조회 로직 추가 필요
        return UserPreferDto.SimilarUserResponse.builder()
                .userId(userPrefer.getUserId())
                .nickname("사용자" + userPrefer.getUserId()) // 임시 값
                .profileImgUrl(null) // 임시 값
                .similarityScore(0.0) // 임시 값
                .preferences(convertToResponse(userPrefer))
                .build();
    }
} 