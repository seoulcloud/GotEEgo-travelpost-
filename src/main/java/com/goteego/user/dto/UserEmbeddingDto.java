package com.goteego.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 임베딩 관련 DTO
 */
public class UserEmbeddingDto {

    /**
     * 임베딩 생성/업데이트 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long userId;
        private List<Float> embedding;
    }

    /**
     * 임베딩 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long userEmbeddingId;
        private Long userId;
        private List<Float> embedding;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

    /**
     * 유사한 사용자 응답 DTO (similarity 포함)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimilarUserResponse {
        private Long userId;
        private List<Float> embedding;
        private Double similarity; // 유사도 값 (0.0 ~ 1.0, 클수록 유사)
        private LocalDateTime createdAt;
    }

    /**
     * 유사도 계산 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimilarityResponse {
        private Long userId1;
        private Long userId2;
        private String method; // "cosine" 또는 "euclidean"
        private Double similarity; // 유사도 값
    }
} 