package com.goteego.travel.controller;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.dto.TravelPostDto;
import com.goteego.travel.service.TravelPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 여행 게시글 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/travel-posts")
@RequiredArgsConstructor
public class TravelPostController {

    private final TravelPostService travelPostService;

    /**
     * 여행 게시글 생성
     * POST /api/travel-posts
     */
    @PostMapping
    public ResponseEntity<TravelPostDto.Response> createTravelPost(
            @RequestBody TravelPostDto.CreateRequest request) {
        log.info("여행 게시글 생성 요청: userId={}, title={}", request.getUserId(), request.getTitle());

        try {
            // TODO: User 객체 조회 로직 필요
            // User user = userService.getUserById(request.getUserId());
            
            TravelPost travelPost = TravelPost.builder()
                    // .user(user) // TODO: User 객체 설정
                    .title(request.getTitle())
                    .content(request.getContent())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .imageUrl(request.getImageUrl())
                    .recuitLimit(request.getRecuitLimit())
                    .postType(request.getPostType())
                    .build();

            TravelPost savedPost = travelPostService.createTravelPost(travelPost);
            TravelPostDto.Response response = convertToResponse(savedPost);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("여행 게시글 생성 실패: userId={}, error={}", request.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 여행 게시글 목록 조회 (다양한 정렬 옵션)
     * GET /api/travel-posts?sort=similarity&order=asc&page=0&size=10
     * 
     * 정렬 옵션:
     * - sort=createdAt&order=desc (최신순)
     * - sort=title&order=asc (ABC순)
     * - sort=similarity&order=asc (선호도순 - 거리 작은 순)
     */
    @GetMapping
    public ResponseEntity<List<TravelPostDto.ListResponse>> getTravelPosts(
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long currentUserId) {
        
        log.info("여행 게시글 목록 조회 요청: sort={}, order={}, page={}, size={}, currentUserId={}", 
                sort, order, page, size, currentUserId);

        try {
            List<TravelPost> travelPosts;
            
            if ("similarity".equals(sort) && currentUserId != null) {
                // 선호도 기반 정렬 (유사도순)
                travelPosts = getTravelPostsBySimilarity(currentUserId, page, size);
            } else {
                // 일반 정렬 (최신순, ABC순)
                travelPosts = getTravelPostsBySort(sort, order, page, size);
            }

            List<TravelPostDto.ListResponse> responses = travelPosts.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("여행 게시글 목록 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 여행 게시글 상세 조회 (조회수 증가)
     * GET /api/travel-posts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TravelPostDto.Response> getTravelPost(@PathVariable Long id) {
        log.info("여행 게시글 상세 조회 요청: id={}", id);

        try {
            Optional<TravelPost> travelPostOpt = travelPostService.getTravelPostWithViewCount(id);
            if (travelPostOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TravelPostDto.Response response = convertToResponse(travelPostOpt.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("여행 게시글 상세 조회 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 여행 게시글 수정
     * PUT /api/travel-posts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TravelPostDto.Response> updateTravelPost(
            @PathVariable Long id,
            @RequestBody TravelPostDto.UpdateRequest request) {
        log.info("여행 게시글 수정 요청: id={}", id);

        try {
            TravelPost updatedPost = TravelPost.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .imageUrl(request.getImageUrl())
                    .recuitLimit(request.getRecuitLimit())
                    .postType(request.getPostType())
                    .build();

            TravelPost savedPost = travelPostService.updateTravelPost(id, updatedPost);
            TravelPostDto.Response response = convertToResponse(savedPost);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("여행 게시글 수정 실패 (게시글 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("여행 게시글 수정 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 여행 게시글 삭제
     * DELETE /api/travel-posts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTravelPost(@PathVariable Long id) {
        log.info("여행 게시글 삭제 요청: id={}", id);

        try {
            travelPostService.deleteTravelPost(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("여행 게시글 삭제 실패 (게시글 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("여행 게시글 삭제 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 모집 완료 처리
     * POST /api/travel-posts/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<TravelPostDto.Response> completeRecruitment(@PathVariable Long id) {
        log.info("모집 완료 처리 요청: id={}", id);

        try {
            TravelPost completedPost = travelPostService.completeRecruitment(id);
            TravelPostDto.Response response = convertToResponse(completedPost);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("모집 완료 처리 실패 (게시글 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("모집 완료 처리 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 모집 재개 처리
     * POST /api/travel-posts/{id}/reopen
     */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<TravelPostDto.Response> reopenRecruitment(@PathVariable Long id) {
        log.info("모집 재개 처리 요청: id={}", id);

        try {
            TravelPost reopenedPost = travelPostService.reopenRecruitment(id);
            TravelPostDto.Response response = convertToResponse(reopenedPost);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("모집 재개 처리 실패 (게시글 없음): id={}, error={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("모집 재개 처리 실패: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 게시글 검색
     * GET /api/travel-posts/search?keyword=제주도
     */
    @GetMapping("/search")
    public ResponseEntity<List<TravelPostDto.ListResponse>> searchTravelPosts(
            @RequestParam String keyword) {
        log.info("게시글 검색 요청: keyword={}", keyword);

        try {
            List<TravelPost> searchResults = travelPostService.searchTravelPostsByKeyword(keyword);
            List<TravelPostDto.ListResponse> responses = searchResults.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("게시글 검색 실패: keyword={}, error={}", keyword, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 진행 중인 여행 조회
     * GET /api/travel-posts/currently-traveling
     */
    @GetMapping("/currently-traveling")
    public ResponseEntity<List<TravelPostDto.ListResponse>> getCurrentlyTravelingPosts() {
        log.info("현재 진행 중인 여행 조회 요청");

        try {
            List<TravelPost> currentlyTravelingPosts = travelPostService.getCurrentlyTravelingPosts();
            List<TravelPostDto.ListResponse> responses = currentlyTravelingPosts.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("현재 진행 중인 여행 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 예정된 여행 조회
     * GET /api/travel-posts/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<TravelPostDto.ListResponse>> getUpcomingTravelPosts() {
        log.info("예정된 여행 조회 요청");

        try {
            List<TravelPost> upcomingTravelPosts = travelPostService.getUpcomingTravelPosts();
            List<TravelPostDto.ListResponse> responses = upcomingTravelPosts.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("예정된 여행 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 정렬 메서드들
    private List<TravelPost> getTravelPostsBySort(String sort, String order, int page, int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, getSortField(sort)));
        
        Page<TravelPost> travelPostPage = travelPostService.getAllTravelPosts(pageable);
        return travelPostPage.getContent();
    }

    private List<TravelPost> getTravelPostsBySimilarity(Long currentUserId, int page, int size) {
        // TODO: pgvector를 사용한 유사도 기반 정렬 구현
        // 1. 현재 사용자의 임베딩 조회
        // 2. 모든 게시글 작성자의 임베딩과 거리 계산
        // 3. 거리 기준으로 정렬 (ASC - 거리 작은 순)
        
        log.info("선호도 기반 정렬 구현 필요: currentUserId={}", currentUserId);
        
        // 임시로 최신순 반환
        return getTravelPostsBySort("createdAt", "desc", page, size);
    }

    private String getSortField(String sort) {
        if (sort == null) {
            return "createdAt";
        }
        
        return switch (sort.toLowerCase()) {
            case "title" -> "title";
            case "viewcount" -> "viewCount";
            case "starttime" -> "startTime";
            default -> "createdAt";
        };
    }

    // DTO 변환 메서드들
    private TravelPostDto.Response convertToResponse(TravelPost travelPost) {
        return TravelPostDto.Response.builder()
                .travelPostId(travelPost.getTravelPostId())
                .userId(travelPost.getUser() != null ? travelPost.getUser().getUserId() : null)
                .userNickname(travelPost.getUser() != null ? travelPost.getUser().getNickname() : null)
                .userProfileImgUrl(travelPost.getUser() != null ? travelPost.getUser().getProfileImgUrl() : null)
                .chatRoomId(travelPost.getChatRoomId())
                .title(travelPost.getTitle())
                .content(travelPost.getContent())
                .startTime(travelPost.getStartTime())
                .endTime(travelPost.getEndTime())
                .imageUrl(travelPost.getImageUrl())
                .recuitLimit(travelPost.getRecuitLimit())
                .viewCount(travelPost.getViewCount())
                .postType(travelPost.getPostType())
                .isAddRecruit(travelPost.getIsAddRecruit())
                .createdAt(travelPost.getCreatedAt())
                .modifiedAt(travelPost.getModifiedAt())
                .travelDuration(travelPost.getTravelDuration())
                .isCurrentlyTraveling(travelPost.isCurrentlyTraveling())
                .isUpcomingTravel(travelPost.isUpcomingTravel())
                .build();
    }

    private TravelPostDto.ListResponse convertToListResponse(TravelPost travelPost) {
        return TravelPostDto.ListResponse.builder()
                .travelPostId(travelPost.getTravelPostId())
                .userId(travelPost.getUser() != null ? travelPost.getUser().getUserId() : null)
                .userNickname(travelPost.getUser() != null ? travelPost.getUser().getNickname() : null)
                .userProfileImgUrl(travelPost.getUser() != null ? travelPost.getUser().getProfileImgUrl() : null)
                .title(travelPost.getTitle())
                .content(travelPost.getContent())
                .startTime(travelPost.getStartTime())
                .endTime(travelPost.getEndTime())
                .imageUrl(travelPost.getImageUrl())
                .recuitLimit(travelPost.getRecuitLimit())
                .viewCount(travelPost.getViewCount())
                .postType(travelPost.getPostType())
                .isAddRecruit(travelPost.getIsAddRecruit())
                .createdAt(travelPost.getCreatedAt())
                .travelDuration(travelPost.getTravelDuration())
                .isCurrentlyTraveling(travelPost.isCurrentlyTraveling())
                .isUpcomingTravel(travelPost.isUpcomingTravel())
                .build();
    }
} 