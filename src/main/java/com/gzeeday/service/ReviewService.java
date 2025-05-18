package com.gzeeday.service;

import com.gzeeday.domain.comment.CommentRepository;
import com.gzeeday.domain.like.LikeRepository;
import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.plan.ConfirmedPlanRepository;
import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.web.dto.review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ConfirmedPlanRepository confirmedPlanRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final RecommendationRepository recommendationRepository;

    // 임시 데이터 추가 메서드
    @PostConstruct
    @Transactional
    public void initSampleData() {
        // 리뷰 데이터가 없는 경우에만 샘플 데이터 추가
        if (reviewRepository.count() == 0) {
            // 기본 사용자 가져오기 또는 생성
            User defaultUser = userRepository.findById(1L).orElseGet(() -> {
                User newUser = User.builder()
                        .name("샘플 사용자")
                        .nickname("샘플닉네임")
                        .username("sample")
                        .password("password")
                        .email("sample@example.com")
                        .phone("010-1234-5678")
                        .role(com.gzeeday.domain.user.Role.USER)
                        .emailVerified(true)
                        .build();
                return userRepository.save(newUser);
            });
            
            // 추천 활동 가져오기 또는 생성
            Recommendation recommendation = recommendationRepository.findById(1L).orElseGet(() -> {
                Recommendation newRecommendation = Recommendation.builder()
                        .title("공원에서 산책하기")
                        .description("가까운 공원에서 30분 동안 산책해보세요. 자연과 함께하는 시간을 가져보세요.")
                        .build();
                return recommendationRepository.save(newRecommendation);
            });
            
            // 확정된 계획 생성
            ConfirmedPlan confirmedPlan = confirmedPlanRepository.findById(1L).orElseGet(() -> {
                ConfirmedPlan newPlan = ConfirmedPlan.builder()
                        .user(defaultUser)
                        .recommendation(recommendation)
                        .confirmedAt(LocalDateTime.now().minusDays(1))
                        .build();
                return confirmedPlanRepository.save(newPlan);
            });
            
            // 리뷰 생성
            Review review = Review.builder()
                    .user(defaultUser)
                    .confirmedPlan(confirmedPlan)
                    .title("공원 산책 후기")
                    .content("오랜만에 공원에서 산책을 했어요. 날씨가 좋아서 기분이 좋았습니다. 다음에도 또 가고 싶네요!")
                    .starRating(5)
                    .build();
            reviewRepository.save(review);
            
            // 두 번째 추천 활동
            Recommendation recommendation2 = recommendationRepository.findById(2L).orElseGet(() -> {
                Recommendation newRecommendation = Recommendation.builder()
                        .title("새로운 요리 도전하기")
                        .description("평소에 해보지 않았던 요리를 한 가지 도전해보세요. 새로운 경험이 될 거예요.")
                        .build();
                return recommendationRepository.save(newRecommendation);
            });
            
            // 두 번째 확정된 계획
            ConfirmedPlan confirmedPlan2 = confirmedPlanRepository.findById(2L).orElseGet(() -> {
                ConfirmedPlan newPlan = ConfirmedPlan.builder()
                        .user(defaultUser)
                        .recommendation(recommendation2)
                        .confirmedAt(LocalDateTime.now().minusDays(2))
                        .build();
                return confirmedPlanRepository.save(newPlan);
            });
            
            // 두 번째 리뷰
            Review review2 = Review.builder()
                    .user(defaultUser)
                    .confirmedPlan(confirmedPlan2)
                    .title("처음으로 파스타 만들어봤어요")
                    .content("유튜브 보면서 까르보나라 파스타를 만들어봤습니다. 생각보다 어렵지 않았고 맛있게 잘 먹었어요. 요리의 즐거움을 느꼈습니다!")
                    .starRating(4)
                    .build();
            reviewRepository.save(review2);
        }
    }

    @Transactional
    public Long save(ReviewDto.RequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 비로그인 사용자를 위한 처리 (userId가 기본값인 경우)
        ConfirmedPlan confirmedPlan;
        if (userId == 1L) {
            confirmedPlan = confirmedPlanRepository.findById(requestDto.getConfirmedPlanId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다."));
        } else {
            confirmedPlan = confirmedPlanRepository.findByIdAndUser(requestDto.getConfirmedPlanId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다."));
        }
        
        // 이미 후기가 작성되었는지 확인 (모든 사용자의 후기 체크)
        if (reviewRepository.existsByConfirmedPlan(confirmedPlan)) {
            throw new IllegalArgumentException("이미 후기가 작성된 확정 계획입니다.");
        }
        
        Review review = requestDto.toEntity(user, confirmedPlan);
        reviewRepository.save(review);
        
        return review.getId();
    }

    @Transactional
    public void update(Long id, ReviewDto.RequestDto requestDto, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다."));
        
        review.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getImageUrl(), requestDto.getStarRating());
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다."));
        
        // 댓글 먼저 삭제 (외래키 제약조건 오류 해결)
        commentRepository.deleteAllByReview(review);
        
        // 좋아요 삭제
        likeRepository.deleteAllByReview(review);
        
        // 리뷰 삭제
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public ReviewDto.ResponseDto findById(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다."));
        
        long likeCount = likeRepository.countByReview(review);
        boolean isAuthor = userId != null && review.getUser().getId().equals(userId);
        boolean isLiked = userId != null && likeRepository.existsByUserAndReview(
                userRepository.findById(userId).orElse(null), review);
        
        return new ReviewDto.ResponseDto(review, likeCount, isAuthor, isLiked);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto.ListResponseDto> findAllPaging(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(review -> {
                    long likeCount = likeRepository.countByReview(review);
                    long commentCount = commentRepository.findAllByReviewOrderByCreatedAtAsc(review).size();
                    return new ReviewDto.ListResponseDto(review, likeCount, commentCount);
                });
    }

    @Transactional(readOnly = true)
    public List<ReviewDto.ListResponseDto> findAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return reviewRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(review -> {
                    long likeCount = likeRepository.countByReview(review);
                    long commentCount = commentRepository.findAllByReviewOrderByCreatedAtAsc(review).size();
                    return new ReviewDto.ListResponseDto(review, likeCount, commentCount);
                })
                .collect(Collectors.toList());
    }
}