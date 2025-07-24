package com.goteego.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 사용자 임베딩 엔티티
 * pgvector를 사용하여 사용자 선호도를 벡터로 저장
 */
@Entity
@Table(name = "user_embeddings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_embedding_id")
    private Long userEmbeddingId;

    @Column(name = "user_id")
    private Long userId;

    /**
     * 사용자 선호도 벡터 (30차원)
     * pgvector의 VECTOR(30) 타입으로 저장
     */
    @Column(name = "user_embedding", columnDefinition = "vector(30)")
    private String userEmbedding; // PostgreSQL pgvector 타입

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // User와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 유사도 계산 결과 값 (임시 필드, DB에 저장되지 않음)
     */
    @Transient
    private Double similarity;

    /**
     * 선호도 배열을 벡터 문자열로 변환
     * pgvector 형식: "[1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0]"
     */
    public void setEmbeddingFromArray(int[] preferenceArray) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < preferenceArray.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(preferenceArray[i]);
        }
        // 30차원으로 맞추기 위해 0으로 패딩
        for (int i = preferenceArray.length; i < 30; i++) {
            sb.append(",0");
        }
        sb.append("]");
        this.userEmbedding = sb.toString();
    }

    /**
     * 벡터 문자열을 배열로 변환
     */
    public int[] getEmbeddingAsArray() {
        if (userEmbedding == null || userEmbedding.isEmpty()) {
            return new int[30];
        }
        
        // "[1,0,1,0,...]" 형식에서 숫자만 추출
        String clean = userEmbedding.replaceAll("[\\[\\]]", "");
        String[] parts = clean.split(",");
        int[] result = new int[30];
        
        for (int i = 0; i < Math.min(parts.length, 30); i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        
        return result;
    }

    // Controller에서 사용하는 메서드들
    public List<Float> getEmbedding() {
        if (userEmbedding == null || userEmbedding.isEmpty()) {
            return List.of();
        }
        
        String clean = userEmbedding.replaceAll("[\\[\\]]", "");
        String[] parts = clean.split(",");
        List<Float> result = new ArrayList<>();
        
        for (int i = 0; i < Math.min(parts.length, 30); i++) {
            result.add(Float.parseFloat(parts[i].trim()));
        }
        
        return result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Builder 메서드 추가
    public static UserEmbeddingBuilder builder() {
        return new UserEmbeddingBuilder();
    }

    public static class UserEmbeddingBuilder {
        private Long userEmbeddingId;
        private User user;
        private String userEmbedding;
        private LocalDateTime createdAt;

        public UserEmbeddingBuilder userEmbeddingId(Long userEmbeddingId) {
            this.userEmbeddingId = userEmbeddingId;
            return this;
        }

        public UserEmbeddingBuilder user(User user) {
            this.user = user;
            return this;
        }

        public UserEmbeddingBuilder embedding(List<Float> embedding) {
            if (embedding != null && !embedding.isEmpty()) {
                this.userEmbedding = embedding.toString();
            }
            return this;
        }

        public UserEmbeddingBuilder userEmbedding(String userEmbedding) {
            this.userEmbedding = userEmbedding;
            return this;
        }

        public UserEmbeddingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserEmbedding build() {
            UserEmbedding userEmbedding = new UserEmbedding();
            userEmbedding.userEmbeddingId = this.userEmbeddingId;
            userEmbedding.user = this.user;
            userEmbedding.userEmbedding = this.userEmbedding;
            userEmbedding.createdAt = this.createdAt;
            return userEmbedding;
        }
    }
} 