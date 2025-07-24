package com.goteego.user.repository;

import com.goteego.user.domain.UserEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 임베딩 Repository
 * pgvector를 사용한 유사도 검색 기능 제공
 */
@Repository
public interface UserEmbeddingRepository extends JpaRepository<UserEmbedding, Long> {

    /**
     * 사용자 ID로 임베딩 조회
     */
    Optional<UserEmbedding> findByUserId(Long userId);

    /**
     * pgvector를 사용한 코사인 유사도 검색
     * 가장 유사한 사용자들을 반환 (상위 N개)
     */
    @Query(value = """
        SELECT ue.user_id, ue.user_embedding, 
               (ue.user_embedding <=> :targetEmbedding) as similarity
        FROM user_embeddings ue
        WHERE ue.user_id != :excludeUserId
        ORDER BY ue.user_embedding <=> :targetEmbedding
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarUsersByCosineDistance(
        @Param("targetEmbedding") String targetEmbedding,
        @Param("excludeUserId") Long excludeUserId,
        @Param("limit") int limit
    );

    /**
     * pgvector를 사용한 유클리드 거리 검색
     */
    @Query(value = """
        SELECT ue.user_id, ue.user_embedding, 
               (ue.user_embedding <-> :targetEmbedding) as distance
        FROM user_embeddings ue
        WHERE ue.user_id != :excludeUserId
        ORDER BY ue.user_embedding <-> :targetEmbedding
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarUsersByEuclideanDistance(
        @Param("targetEmbedding") String targetEmbedding,
        @Param("excludeUserId") Long excludeUserId,
        @Param("limit") int limit
    );

    /**
     * 특정 임베딩과 유사도가 임계값 이상인 사용자들 조회
     */
    @Query(value = """
        SELECT ue.user_id, ue.user_embedding, 
               (ue.user_embedding <=> :targetEmbedding) as similarity
        FROM user_embeddings ue
        WHERE ue.user_id != :excludeUserId
        AND (ue.user_embedding <=> :targetEmbedding) < :threshold
        ORDER BY ue.user_embedding <=> :targetEmbedding
        """, nativeQuery = true)
    List<Object[]> findUsersAboveSimilarityThreshold(
        @Param("targetEmbedding") String targetEmbedding,
        @Param("excludeUserId") Long excludeUserId,
        @Param("threshold") double threshold
    );

    /**
     * 특정 사용자와 가장 유사한 사용자 1명 조회
     */
    @Query(value = """
        SELECT ue.user_id, ue.user_embedding, 
               (ue.user_embedding <=> :targetEmbedding) as similarity
        FROM user_embeddings ue
        WHERE ue.user_id != :excludeUserId
        ORDER BY ue.user_embedding <=> :targetEmbedding
        LIMIT 1
        """, nativeQuery = true)
    Optional<Object[]> findMostSimilarUser(
        @Param("targetEmbedding") String targetEmbedding,
        @Param("excludeUserId") Long excludeUserId
    );

    /**
     * 임베딩이 존재하는 사용자 수 조회
     */
    @Query("SELECT COUNT(ue) FROM UserEmbedding ue WHERE ue.userEmbedding IS NOT NULL")
    long countUsersWithEmbeddings();

    /**
     * 특정 사용자와 유사한 사용자들의 평균 유사도 계산
     */
    @Query(value = """
        SELECT AVG(ue.user_embedding <=> :targetEmbedding) as avg_similarity
        FROM user_embeddings ue
        WHERE ue.user_id != :excludeUserId
        """, nativeQuery = true)
    Double getAverageSimilarity(
        @Param("targetEmbedding") String targetEmbedding,
        @Param("excludeUserId") Long excludeUserId
    );

    /**
     * 사용자 ID로 임베딩 삭제
     */
    void deleteByUserId(Long userId);
} 