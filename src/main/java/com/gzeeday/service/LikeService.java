package com.gzeeday.service;

import com.gzeeday.domain.like.Like;
import com.gzeeday.domain.like.LikeRepository;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.web.dto.like.LikeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 좋아요 서비스
 * - 후기에 대한 좋아요 관련 기능을 처리하는 서비스 클래스입니다.
 * - 좋아요 추가/취소(토글), 목록 조회 기능을 제공합니다.
 */
@Service // 스프링이 서비스 계층으로 인식하도록 하는 어노테이션
@RequiredArgsConstructor // final 필드를 초기화하는 생성자를 자동으로 생성
public class LikeService {

    // 필요한 Repository 객체들을 주입받음
    private final LikeRepository likeRepository; // 좋아요 데이터 접근
    private final ReviewRepository reviewRepository; // 후기 데이터 접근
    
    // 자주 사용되는 상수 정의
    private static final String DEFAULT_AUTHOR = "익명"; // 작성자가 없을 때 사용하는 기본값

    /**
     * 좋아요 토글 기능 (좋아요 추가/취소)
     * - 이미 좋아요한 상태면 취소, 아니면 추가합니다.
     * 
     * @param reviewId 좋아요 대상 후기 ID
     * @param authorName 좋아요 작성자 이름
     * @return 처리 결과 DTO (좋아요 상태, 개수 등)
     * @throws IllegalArgumentException 해당 ID의 후기가 없는 경우
     */
    @Transactional
    public LikeDto.ResponseDto toggleLike(Long reviewId, String authorName) {
        // 작성자 이름이 없으면 기본값 사용
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = DEFAULT_AUTHOR;
        }
        
        // 1. 후기 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. ID: " + reviewId));
        
        // 2. 현재 사용자가 좋아요 했는지 확인
        boolean isLiked = likeRepository.existsByAuthorNameAndReview(authorName, review);
        
        // 3. 좋아요 상태 토글 (추가 또는 취소)
        if (isLiked) {
            // 이미 공감한 경우, 공감 취소
            likeRepository.deleteByAuthorNameAndReview(authorName, review);
        } else {
            // 공감하지 않은 경우, 공감 추가
            Like like = Like.builder()
                    .authorName(authorName)
                    .review(review)
                    .build();
            likeRepository.save(like);
        }
        
        // 4. 좋아요 수 조회
        long likeCount = likeRepository.countByReview(review);
        
        // 5. 응답 DTO 생성 및 반환
        return LikeDto.ResponseDto.builder()
                .reviewId(reviewId)
                .likeCount(likeCount)
                .isLiked(!isLiked) // 토글 후 상태를 반환
                .build();
    }

    /**
     * 특정 사용자가 좋아요한 모든 후기를 조회합니다.
     * 
     * @param authorName 작성자 이름
     * @return 좋아요한 후기 목록
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (성능 최적화)
    public List<Review> findAllLikedReviewsByAuthorName(String authorName) {
        // 작성자 이름이 없으면 기본값 사용
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = DEFAULT_AUTHOR;
        }
        
        // 1. 사용자가 좋아요한 모든 좋아요 엔티티 조회 (생성일 기준 내림차순)
        List<Like> likes = likeRepository.findAllByAuthorNameOrderByCreatedAtDesc(authorName);
        
        // 2. 결과를 담을 후기 리스트 생성
        List<Review> reviews = new ArrayList<>();
        
        // 3. 각 좋아요 엔티티에서 후기 객체를 추출하여 리스트에 추가
        for (Like like : likes) {
            reviews.add(like.getReview());
        }
        
        return reviews;
    }
} 