package com.goteego.user.controller;

import com.goteego.user.domain.UserEmbedding;
import com.goteego.user.dto.UserEmbeddingDto;
import com.goteego.user.service.UserEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 임베딩 추천 API Controller
 * pgvector를 사용한 유사도 기반 추천 기능
 */
@Slf4j
@RestController
@RequestMapping("/api/user-embeddings")
@RequiredArgsConstructor
public class UserEmbeddingController {

    private final UserEmbeddingService userEmbeddingService;

    /**
     * 사용자 임베딩 생성/업데이트
     * POST /api/user-embeddings
     */
    @PostMapping
    public ResponseEntity<UserEmbeddingDto.Response> createOrUpdateUserEmbedding(
            @RequestBody UserEmbeddingDto.CreateRequest request) {
        log.info("사용자 임베딩 생성/업데이트 요청: userId={}", request.getUserId());

        try {
            UserEmbedding userEmbedding = UserEmbedding.builder()
                    .userId(request.getUserId())
                    .embedding(request.getEmbedding())
                    .build();

            UserEmbedding savedEmbedding = userEmbeddingService.createOrUpdateUserEmbedding(userEmbedding);
            UserEmbeddingDto.Response response = convertToResponse(savedEmbedding);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("사용자 임베딩 생성/업데이트 실패: userId={}, error={}", request.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자 임베딩 조회
     * GET /api/user-embeddings/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserEmbeddingDto.Response> getUserEmbedding(@PathVariable Long userId) {
        log.info("사용자 임베딩 조회 요청: userId={}", userId);

        try {
            Optional<UserEmbedding> userEmbeddingOpt = userEmbeddingService.getUserEmbedding(userId);
            if (userEmbeddingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserEmbeddingDto.Response response = convertToResponse(userEmbeddingOpt.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 임베딩 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 유사한 사용자 추천 (Cosine Distance)
     * GET /api/user-embeddings/{userId}/similar-users?limit=10
     */
    @GetMapping("/{userId}/similar-users")
    public ResponseEntity<List<UserEmbeddingDto.SimilarUserResponse>> getSimilarUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("유사한 사용자 추천 요청: userId={}, limit={}", userId, limit);

        try {
            List<UserEmbedding> similarUsers = userEmbeddingService.findSimilarUsersByCosineDistance(userId, limit);
            List<UserEmbeddingDto.SimilarUserResponse> responses = similarUsers.stream()
                    .map(this::convertToSimilarUserResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("유사한 사용자 추천 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 유사한 사용자 추천 (Euclidean Distance)
     * GET /api/user-embeddings/{userId}/similar-users-euclidean?limit=10
     */
    @GetMapping("/{userId}/similar-users-euclidean")
    public ResponseEntity<List<UserEmbeddingDto.SimilarUserResponse>> getSimilarUsersByEuclidean(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("유사한 사용자 추천 요청 (Euclidean): userId={}, limit={}", userId, limit);

        try {
            List<UserEmbedding> similarUsers = userEmbeddingService.findSimilarUsersByEuclideanDistance(userId, limit);
            List<UserEmbeddingDto.SimilarUserResponse> responses = similarUsers.stream()
                    .map(this::convertToSimilarUserResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("유사한 사용자 추천 실패 (Euclidean): userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자 임베딩 삭제
     * DELETE /api/user-embeddings/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserEmbedding(@PathVariable Long userId) {
        log.info("사용자 임베딩 삭제 요청: userId={}", userId);

        try {
            userEmbeddingService.deleteUserEmbedding(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("사용자 임베딩 삭제 실패 (임베딩 없음): userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("사용자 임베딩 삭제 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 모든 사용자 임베딩 조회
     * GET /api/user-embeddings
     */
    @GetMapping
    public ResponseEntity<List<UserEmbeddingDto.Response>> getAllUserEmbeddings() {
        log.info("모든 사용자 임베딩 조회 요청");

        try {
            List<UserEmbedding> allEmbeddings = userEmbeddingService.getAllUserEmbeddings();
            List<UserEmbeddingDto.Response> responses = allEmbeddings.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("모든 사용자 임베딩 조회 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자 선호도로부터 임베딩 생성
     * POST /api/user-embeddings/{userId}/generate-from-preferences
     */
    @PostMapping("/{userId}/generate-from-preferences")
    public ResponseEntity<UserEmbeddingDto.Response> generateEmbeddingFromPreferences(@PathVariable Long userId) {
        log.info("사용자 선호도로부터 임베딩 생성 요청: userId={}", userId);

        try {
            UserEmbedding generatedEmbedding = userEmbeddingService.generateEmbeddingFromUserPreferences(userId);
            UserEmbeddingDto.Response response = convertToResponse(generatedEmbedding);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("임베딩 생성 실패 (사용자 선호도 없음): userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("임베딩 생성 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 두 사용자 간 유사도 계산
     * GET /api/user-embeddings/similarity?userId1=1&userId2=2&method=cosine
     */
    @GetMapping("/similarity")
    public ResponseEntity<UserEmbeddingDto.SimilarityResponse> calculateSimilarity(
            @RequestParam Long userId1,
            @RequestParam Long userId2,
            @RequestParam(defaultValue = "cosine") String method) {
        log.info("사용자 간 유사도 계산 요청: userId1={}, userId2={}, method={}", userId1, userId2, method);

        try {
            double similarity;
            if ("euclidean".equalsIgnoreCase(method)) {
                similarity = userEmbeddingService.calculateEuclideanDistance(userId1, userId2);
            } else {
                similarity = userEmbeddingService.calculateCosineDistance(userId1, userId2);
            }

            UserEmbeddingDto.SimilarityResponse response = UserEmbeddingDto.SimilarityResponse.builder()
                    .userId1(userId1)
                    .userId2(userId2)
                    .method(method)
                    .similarity(similarity)
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("유사도 계산 실패 (사용자 없음): userId1={}, userId2={}, error={}", userId1, userId2, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("유사도 계산 실패: userId1={}, userId2={}, error={}", userId1, userId2, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTO 변환 메서드들
    private UserEmbeddingDto.Response convertToResponse(UserEmbedding userEmbedding) {
        return UserEmbeddingDto.Response.builder()
                .userEmbeddingId(userEmbedding.getUserEmbeddingId())
                .userId(userEmbedding.getUserId())
                .embedding(userEmbedding.getEmbedding())
                .createdAt(userEmbedding.getCreatedAt())
                .modifiedAt(userEmbedding.getModifiedAt())
                .build();
    }

    private UserEmbeddingDto.SimilarUserResponse convertToSimilarUserResponse(UserEmbedding userEmbedding) {
        return UserEmbeddingDto.SimilarUserResponse.builder()
                .userId(userEmbedding.getUserId())
                .embedding(userEmbedding.getEmbedding())
                .similarity(userEmbedding.getSimilarity()) // similarity 값 포함
                .createdAt(userEmbedding.getCreatedAt())
                .build();
    }
} 