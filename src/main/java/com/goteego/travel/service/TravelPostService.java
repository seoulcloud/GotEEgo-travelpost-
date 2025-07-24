package com.goteego.travel.service;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.repository.TravelPostRepository;
import com.goteego.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 여행 게시글 서비스
 * 게시글 CRUD, 사진 첨부, 조회수 관리, 채팅방 연동
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelPostService {

    private final TravelPostRepository travelPostRepository;

    /**
     * 여행 게시글 생성
     */
    @Transactional
    public TravelPost createTravelPost(TravelPost travelPost) {
        log.info("여행 게시글 생성: userId={}, title={}", 
                travelPost.getUser().getUserId(), travelPost.getTitle());
        
        // 기본값 설정
        travelPost.setViewCount(0L);
        travelPost.setIsAddRecruit(false);
        
        TravelPost savedPost = travelPostRepository.save(travelPost);
        
        // TODO: 채팅방 자동 생성 로직 추가
        // createChatRoomForPost(savedPost);
        
        return savedPost;
    }

    /**
     * 여행 게시글 수정
     */
    @Transactional
    public TravelPost updateTravelPost(Long travelPostId, TravelPost updatedPost) {
        log.info("여행 게시글 수정: travelPostId={}", travelPostId);
        
        Optional<TravelPost> existingPostOpt = travelPostRepository.findByTravelPostId(travelPostId);
        if (existingPostOpt.isEmpty()) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: travelPostId=" + travelPostId);
        }

        TravelPost existingPost = existingPostOpt.get();
        
        // 수정 가능한 필드들만 업데이트
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setStartTime(updatedPost.getStartTime());
        existingPost.setEndTime(updatedPost.getEndTime());
        existingPost.setImageUrl(updatedPost.getImageUrl());
        existingPost.setRecuitLimit(updatedPost.getRecuitLimit());
        existingPost.setPostType(updatedPost.getPostType());
        
        return travelPostRepository.save(existingPost);
    }

    /**
     * 여행 게시글 삭제
     */
    @Transactional
    public void deleteTravelPost(Long travelPostId) {
        log.info("여행 게시글 삭제: travelPostId={}", travelPostId);
        
        if (!travelPostRepository.existsById(travelPostId)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: travelPostId=" + travelPostId);
        }
        
        // TODO: 연관된 채팅방, 참가 신청 등도 함께 삭제
        travelPostRepository.deleteById(travelPostId);
    }

    /**
     * 여행 게시글 조회 (조회수 증가)
     */
    @Transactional
    public Optional<TravelPost> getTravelPostWithViewCount(Long travelPostId) {
        log.info("여행 게시글 조회 (조회수 증가): travelPostId={}", travelPostId);
        
        Optional<TravelPost> postOpt = travelPostRepository.findByTravelPostId(travelPostId);
        if (postOpt.isPresent()) {
            TravelPost post = postOpt.get();
            post.incrementViewCount();
            travelPostRepository.save(post);
        }
        
        return postOpt;
    }

    /**
     * 여행 게시글 조회 (조회수 증가 없음)
     */
    public Optional<TravelPost> getTravelPost(Long travelPostId) {
        log.info("여행 게시글 조회: travelPostId={}", travelPostId);
        return travelPostRepository.findByTravelPostId(travelPostId);
    }

    /**
     * 모든 여행 게시글 조회 (페이징)
     */
    public Page<TravelPost> getAllTravelPosts(Pageable pageable) {
        log.info("모든 여행 게시글 조회 (페이징)");
        return travelPostRepository.findAll(pageable);
    }

    /**
     * 사용자별 게시글 조회
     */
    public List<TravelPost> getTravelPostsByUser(User user) {
        log.info("사용자별 게시글 조회: userId={}", user.getUserId());
        return travelPostRepository.findByUser(user);
    }

    /**
     * 게시글 타입별 조회
     */
    public List<TravelPost> getTravelPostsByType(TravelPost.PostType postType) {
        log.info("게시글 타입별 조회: postType={}", postType);
        return travelPostRepository.findByPostType(postType);
    }

    /**
     * 모집 상태별 조회
     */
    public List<TravelPost> getTravelPostsByRecruitmentStatus(Boolean isAddRecruit) {
        log.info("모집 상태별 조회: isAddRecruit={}", isAddRecruit);
        return travelPostRepository.findByIsAddRecruit(isAddRecruit);
    }

    /**
     * 제목으로 검색
     */
    public List<TravelPost> searchTravelPostsByTitle(String title) {
        log.info("제목으로 검색: title={}", title);
        return travelPostRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * 내용으로 검색
     */
    public List<TravelPost> searchTravelPostsByContent(String content) {
        log.info("내용으로 검색: content={}", content);
        return travelPostRepository.findByContentContainingIgnoreCase(content);
    }

    /**
     * 제목 또는 내용으로 검색
     */
    public List<TravelPost> searchTravelPostsByKeyword(String keyword) {
        log.info("키워드로 검색: keyword={}", keyword);
        return travelPostRepository.findByTitleOrContentContainingIgnoreCase(keyword);
    }

    /**
     * 날짜 범위로 조회
     */
    public List<TravelPost> getTravelPostsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("날짜 범위로 조회: startDate={}, endDate={}", startDate, endDate);
        return travelPostRepository.findByStartTimeBetween(startDate, endDate);
    }

    /**
     * 현재 진행 중인 여행 조회
     */
    public List<TravelPost> getCurrentlyTravelingPosts() {
        log.info("현재 진행 중인 여행 조회");
        return travelPostRepository.findCurrentlyTravelingPosts(LocalDate.now());
    }

    /**
     * 예정된 여행 조회
     */
    public List<TravelPost> getUpcomingTravelPosts() {
        log.info("예정된 여행 조회");
        return travelPostRepository.findUpcomingTravelPosts(LocalDate.now());
    }

    /**
     * 조회수 기준 정렬 조회
     */
    public List<TravelPost> getTravelPostsOrderByViewCount() {
        log.info("조회수 기준 정렬 조회");
        return travelPostRepository.findAllOrderByViewCountDesc();
    }

    /**
     * 최신순 정렬 조회
     */
    public List<TravelPost> getTravelPostsOrderByCreatedAt() {
        log.info("최신순 정렬 조회");
        return travelPostRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 여행 시작일 기준 정렬 조회
     */
    public List<TravelPost> getTravelPostsOrderByStartTime() {
        log.info("여행 시작일 기준 정렬 조회");
        return travelPostRepository.findAllOrderByStartTimeAsc();
    }

    /**
     * 모집 완료 처리
     */
    @Transactional
    public TravelPost completeRecruitment(Long travelPostId) {
        log.info("모집 완료 처리: travelPostId={}", travelPostId);
        
        Optional<TravelPost> postOpt = travelPostRepository.findByTravelPostId(travelPostId);
        if (postOpt.isEmpty()) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: travelPostId=" + travelPostId);
        }

        TravelPost post = postOpt.get();
        post.completeRecruitment();
        return travelPostRepository.save(post);
    }

    /**
     * 모집 재개 처리
     */
    @Transactional
    public TravelPost reopenRecruitment(Long travelPostId) {
        log.info("모집 재개 처리: travelPostId={}", travelPostId);
        
        Optional<TravelPost> postOpt = travelPostRepository.findByTravelPostId(travelPostId);
        if (postOpt.isEmpty()) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: travelPostId=" + travelPostId);
        }

        TravelPost post = postOpt.get();
        post.reopenRecruitment();
        return travelPostRepository.save(post);
    }

    /**
     * 채팅방 ID로 게시글 조회
     */
    public Optional<TravelPost> getTravelPostByChatRoomId(Long chatRoomId) {
        log.info("채팅방 ID로 게시글 조회: chatRoomId={}", chatRoomId);
        return travelPostRepository.findByChatRoomId(chatRoomId);
    }

    /**
     * 통계 조회
     */
    public long countByPostType(TravelPost.PostType postType) {
        return travelPostRepository.countByPostType(postType);
    }

    public long countByRecruitmentStatus(Boolean isAddRecruit) {
        return travelPostRepository.countByRecruitmentStatus(isAddRecruit);
    }

    public Double getAverageViewCount() {
        return travelPostRepository.getAverageViewCount();
    }

    /**
     * 게시글 존재 여부 확인
     */
    public boolean existsByTravelPostId(Long travelPostId) {
        return travelPostRepository.existsById(travelPostId);
    }
} 