package com.gzeeday.service;

import com.gzeeday.domain.like.Like;
import com.gzeeday.domain.like.LikeRepository;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.web.dto.like.LikeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeDto.ResponseDto toggleLike(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        boolean isLiked = likeRepository.existsByUserAndReview(user, review);
        
        if (isLiked) {
            // 이미 공감한 경우, 공감 취소
            likeRepository.deleteByUserAndReview(user, review);
        } else {
            // 공감하지 않은 경우, 공감 추가
            Like like = Like.builder()
                    .user(user)
                    .review(review)
                    .build();
            likeRepository.save(like);
        }
        
        long likeCount = likeRepository.countByReview(review);
        
        return LikeDto.ResponseDto.builder()
                .reviewId(reviewId)
                .likeCount(likeCount)
                .isLiked(!isLiked)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Review> findAllLikedReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return likeRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(Like::getReview)
                .collect(Collectors.toList());
    }
} 