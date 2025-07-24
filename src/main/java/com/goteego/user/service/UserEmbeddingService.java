package com.goteego.user.service;

import com.goteego.user.domain.UserEmbedding;
import com.goteego.user.domain.UserPrefer;
import com.goteego.user.repository.UserEmbeddingRepository;
import com.goteego.user.repository.UserPreferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 임베딩 서비스
 * pgvector를 사용한 유사도 기반 추천 시스템
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserEmbeddingService {

    private final UserEmbeddingRepository userEmbeddingRepository;
    private final UserPreferRepository userPreferRepository;

    /**
     * 사용자 임베딩 저장/수정
     */
    @Transactional
    public UserEmbedding saveUserEmbedding(UserEmbedding userEmbedding) {
        log.info("사용자 임베딩 저장/수정: userId={}", userEmbedding.getUserId());
        return userEmbeddingRepository.save(userEmbedding);
    }

    /**
     * 사용자 선호도를 기반으로 임베딩 생성 및 저장
     */
    @Transactional
    public UserEmbedding createEmbeddingFromPreference(Long userId) {
        log.info("사용자 선호도 기반 임베딩 생성: userId={}", userId);
        
        Optional<UserPrefer> userPreferOpt = userPreferRepository.findByUserId(userId);
        if (userPreferOpt.isEmpty()) {
            throw new IllegalArgumentException("사용자 선호도가 존재하지 않습니다: userId=" + userId);
        }

        UserPrefer userPrefer = userPreferOpt.get();
        int[] preferenceArray = userPrefer.getPreferenceArray();
        
        UserEmbedding userEmbedding = UserEmbedding.builder()
                .userId(userId)
                .build();
        
        userEmbedding.setEmbeddingFromArray(preferenceArray);
        return userEmbeddingRepository.save(userEmbedding);
    }

    /**
     * 사용자 ID로 임베딩 조회
     */
    public Optional<UserEmbedding> getUserEmbeddingByUserId(Long userId) {
        log.info("사용자 임베딩 조회: userId={}", userId);
        return userEmbeddingRepository.findByUserId(userId);
    }

    /**
     * pgvector를 사용한 유사한 사용자 추천 (코사인 유사도) - similarity 포함
     */
    public List<UserEmbedding> findSimilarUsersByCosineDistance(Long userId, int limit) {
        log.info("코사인 유사도 기반 유사 사용자 추천: userId={}, limit={}", userId, limit);
        
        Optional<UserEmbedding> userEmbeddingOpt = getUserEmbeddingByUserId(userId);
        if (userEmbeddingOpt.isEmpty()) {
            log.warn("사용자 임베딩이 존재하지 않습니다: userId={}", userId);
            return List.of();
        }

        String targetEmbedding = userEmbeddingOpt.get().getUserEmbedding();
        List<Object[]> results = userEmbeddingRepository.findSimilarUsersByCosineDistance(
                targetEmbedding, userId, limit
        );

        return results.stream()
                .map(result -> {
                    Long resultUserId = (Long) result[0];
                    Double distance = (Double) result[1];
                    
                    UserEmbedding userEmbedding = userEmbeddingRepository.findByUserId(resultUserId).orElse(null);
                    if (userEmbedding != null) {
                        // distance를 similarity로 변환 (1 - distance)
                        double similarity = 1.0 - distance;
                        userEmbedding.setSimilarity(similarity);
                    }
                    return userEmbedding;
                })
                .filter(userEmbedding -> userEmbedding != null)
                .collect(Collectors.toList());
    }

    /**
     * pgvector를 사용한 유사한 사용자 추천 (코사인 유사도) - 기존 메서드 (호환성)
     */
    public List<Long> getSimilarUsersByCosineDistance(Long userId, int limit) {
        return findSimilarUsersByCosineDistance(userId, limit).stream()
                .map(UserEmbedding::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * pgvector를 사용한 유사한 사용자 추천 (유클리드 거리) - similarity 포함
     */
    public List<UserEmbedding> findSimilarUsersByEuclideanDistance(Long userId, int limit) {
        log.info("유클리드 거리 기반 유사 사용자 추천: userId={}, limit={}", userId, limit);
        
        Optional<UserEmbedding> userEmbeddingOpt = getUserEmbeddingByUserId(userId);
        if (userEmbeddingOpt.isEmpty()) {
            log.warn("사용자 임베딩이 존재하지 않습니다: userId={}", userId);
            return List.of();
        }

        String targetEmbedding = userEmbeddingOpt.get().getUserEmbedding();
        List<Object[]> results = userEmbeddingRepository.findSimilarUsersByEuclideanDistance(
                targetEmbedding, userId, limit
        );

        return results.stream()
                .map(result -> {
                    Long resultUserId = (Long) result[0];
                    Double distance = (Double) result[1];
                    
                    UserEmbedding userEmbedding = userEmbeddingRepository.findByUserId(resultUserId).orElse(null);
                    if (userEmbedding != null) {
                        // distance를 similarity로 변환 (1 / (1 + distance))
                        double similarity = 1.0 / (1.0 + distance);
                        userEmbedding.setSimilarity(similarity);
                    }
                    return userEmbedding;
                })
                .filter(userEmbedding -> userEmbedding != null)
                .collect(Collectors.toList());
    }

    /**
     * pgvector를 사용한 유사한 사용자 추천 (유클리드 거리) - 기존 메서드 (호환성)
     */
    public List<Long> getSimilarUsersByEuclideanDistance(Long userId, int limit) {
        return findSimilarUsersByEuclideanDistance(userId, limit).stream()
                .map(UserEmbedding::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * 특정 임계값 이상의 유사도를 가진 사용자들 조회
     */
    public List<Long> getUsersAboveSimilarityThreshold(Long userId, double threshold) {
        log.info("임계값 이상 유사도 사용자 조회: userId={}, threshold={}", userId, threshold);
        
        Optional<UserEmbedding> userEmbeddingOpt = getUserEmbeddingByUserId(userId);
        if (userEmbeddingOpt.isEmpty()) {
            log.warn("사용자 임베딩이 존재하지 않습니다: userId={}", userId);
            return List.of();
        }

        String targetEmbedding = userEmbeddingOpt.get().getUserEmbedding();
        List<Object[]> results = userEmbeddingRepository.findUsersAboveSimilarityThreshold(
                targetEmbedding, userId, threshold
        );

        return results.stream()
                .map(result -> (Long) result[0])
                .collect(Collectors.toList());
    }

    /**
     * 가장 유사한 사용자 1명 조회
     */
    public Optional<Long> getMostSimilarUser(Long userId) {
        log.info("가장 유사한 사용자 조회: userId={}", userId);
        
        Optional<UserEmbedding> userEmbeddingOpt = getUserEmbeddingByUserId(userId);
        if (userEmbeddingOpt.isEmpty()) {
            log.warn("사용자 임베딩이 존재하지 않습니다: userId={}", userId);
            return Optional.empty();
        }

        String targetEmbedding = userEmbeddingOpt.get().getUserEmbedding();
        Optional<Object[]> result = userEmbeddingRepository.findMostSimilarUser(
                targetEmbedding, userId
        );

        return result.map(obj -> (Long) obj[0]);
    }

    /**
     * 사용자 임베딩 삭제
     */
    @Transactional
    public void deleteUserEmbedding(Long userId) {
        log.info("사용자 임베딩 삭제: userId={}", userId);
        userEmbeddingRepository.deleteById(userId);
    }

    /**
     * 임베딩이 존재하는 사용자 수 조회
     */
    public long countUsersWithEmbeddings() {
        return userEmbeddingRepository.countUsersWithEmbeddings();
    }

    /**
     * 특정 사용자와의 평균 유사도 계산
     */
    public Optional<Double> getAverageSimilarity(Long userId) {
        log.info("평균 유사도 계산: userId={}", userId);
        
        Optional<UserEmbedding> userEmbeddingOpt = getUserEmbeddingByUserId(userId);
        if (userEmbeddingOpt.isEmpty()) {
            log.warn("사용자 임베딩이 존재하지 않습니다: userId={}", userId);
            return Optional.empty();
        }

        String targetEmbedding = userEmbeddingOpt.get().getUserEmbedding();
        Double avgSimilarity = userEmbeddingRepository.getAverageSimilarity(
                targetEmbedding, userId
        );

        return Optional.ofNullable(avgSimilarity);
    }

    /**
     * 모든 사용자의 임베딩을 일괄 업데이트
     */
    @Transactional
    public void updateAllEmbeddings() {
        log.info("모든 사용자 임베딩 일괄 업데이트 시작");
        
        List<UserPrefer> allUserPrefers = userPreferRepository.findAll();
        
        for (UserPrefer userPrefer : allUserPrefers) {
            try {
                createEmbeddingFromPreference(userPrefer.getUserId());
                log.debug("임베딩 업데이트 완료: userId={}", userPrefer.getUserId());
            } catch (Exception e) {
                log.error("임베딩 업데이트 실패: userId={}, error={}", userPrefer.getUserId(), e.getMessage());
            }
        }
        
        log.info("모든 사용자 임베딩 일괄 업데이트 완료: 총 {}명", allUserPrefers.size());
    }

    /**
     * 사용자 임베딩 존재 여부 확인
     */
    public boolean existsByUserId(Long userId) {
        return userEmbeddingRepository.existsById(userId);
    }

    // Controller에서 사용하는 메서드들 추가
    /**
     * 사용자 임베딩 생성/업데이트
     */
    @Transactional
    public UserEmbedding createOrUpdateUserEmbedding(UserEmbedding userEmbedding) {
        log.info("사용자 임베딩 생성/업데이트: userId={}", userEmbedding.getUserId());
        return userEmbeddingRepository.save(userEmbedding);
    }

    /**
     * 사용자 임베딩 조회
     */
    public Optional<UserEmbedding> getUserEmbedding(Long userId) {
        return getUserEmbeddingByUserId(userId);
    }

    /**
     * 모든 사용자 임베딩 조회
     */
    public List<UserEmbedding> getAllUserEmbeddings() {
        return userEmbeddingRepository.findAll();
    }

    /**
     * 사용자 선호도로부터 임베딩 생성
     */
    @Transactional
    public UserEmbedding generateEmbeddingFromUserPreferences(Long userId) {
        return createEmbeddingFromPreference(userId);
    }



    /**
     * 코사인 거리 계산
     */
    public double calculateCosineDistance(Long userId1, Long userId2) {
        Optional<UserEmbedding> embedding1 = getUserEmbeddingByUserId(userId1);
        Optional<UserEmbedding> embedding2 = getUserEmbeddingByUserId(userId2);
        
        if (embedding1.isEmpty() || embedding2.isEmpty()) {
            throw new IllegalArgumentException("사용자 임베딩이 존재하지 않습니다");
        }
        
        // TODO: 실제 코사인 거리 계산 로직 구현
        return 0.0;
    }

    /**
     * 유클리드 거리 계산
     */
    public double calculateEuclideanDistance(Long userId1, Long userId2) {
        Optional<UserEmbedding> embedding1 = getUserEmbeddingByUserId(userId1);
        Optional<UserEmbedding> embedding2 = getUserEmbeddingByUserId(userId2);
        
        if (embedding1.isEmpty() || embedding2.isEmpty()) {
            throw new IllegalArgumentException("사용자 임베딩이 존재하지 않습니다");
        }
        
        // TODO: 실제 유클리드 거리 계산 로직 구현
        return 0.0;
    }
} 