package com.gzeeday.service;

import com.gzeeday.domain.comment.CommentRepository;
import com.gzeeday.domain.like.LikeRepository;
import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.plan.ConfirmedPlanRepository;
import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.web.dto.review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 후기 서비스
 * - 사용자가 작성한 활동 후기를 관리하는 비즈니스 로직을 담당합니다.
 * - 후기 생성, 조회, 수정, 삭제 기능을 제공합니다.
 * - 초기 샘플 데이터 생성 기능도 포함합니다.
 */
@Service // 스프링이 서비스 계층으로 인식하도록 하는 어노테이션
@RequiredArgsConstructor // final 필드를 초기화하는 생성자를 자동으로 생성
public class ReviewService {

    // 필요한 Repository 객체들을 주입받음
    private final ReviewRepository reviewRepository; // 후기 데이터 접근
    private final ConfirmedPlanRepository confirmedPlanRepository; // 확정 계획 데이터 접근
    private final LikeRepository likeRepository; // 좋아요 데이터 접근
    private final CommentRepository commentRepository; // 댓글 데이터 접근
    private final RecommendationRepository recommendationRepository; // 추천 활동 데이터 접근
    
    // 자주 사용되는 상수 정의
    private static final String DEFAULT_AUTHOR = "익명"; // 작성자가 없을 때 사용하는 기본값
    private static final String SYSTEM_AUTHOR = "시스템"; // 시스템이 생성한 데이터의 작성자

    /**
     * 초기 샘플 데이터 생성 메서드
     * - 애플리케이션 시작 시 자동으로 실행됩니다(@PostConstruct).
     * - 데이터베이스에 후기가 없는 경우에만 샘플 데이터를 생성합니다.
     */
    @PostConstruct // 스프링 빈 초기화 후 자동 실행
    @Transactional // 데이터베이스 트랜잭션 관리
    public void initSampleData() {
        // 리뷰 데이터가 없는 경우에만 샘플 데이터 추가
        if (reviewRepository.count() == 0) {
            String sampleUser = "샘플 사용자";
            
            // 첫 번째 샘플 데이터 생성
            createSampleReview(
                1L, // ID
                "공원에서 산책하기", // 추천 활동 제목
                "가까운 공원에서 30분 동안 산책해보세요. 자연과 함께하는 시간을 가져보세요.", // 추천 활동 설명
                "공원 산책 후기", // 후기 제목
                "오랜만에 공원에서 산책을 했어요. 날씨가 좋아서 기분이 좋았습니다. 다음에도 또 가고 싶네요!", // 후기 내용
                sampleUser, // 작성자
                5 // 별점
            );
                
            // 두 번째 샘플 데이터 생성
            createSampleReview(
                2L, 
                "새로운 요리 도전하기", 
                "평소에 해보지 않았던 요리를 한 가지 도전해보세요. 새로운 경험이 될 거예요.",
                "처음으로 파스타 만들어봤어요",
                "유튜브 보면서 까르보나라 파스타를 만들어봤습니다. 생각보다 어렵지 않았고 맛있게 잘 먹었어요. 요리의 즐거움을 느꼈습니다!",
                sampleUser,
                4
            );
            
            System.out.println("샘플 후기 데이터가 생성되었습니다.");
        }
    }
    
    /**
     * 샘플 후기 데이터 생성을 도와주는 헬퍼 메서드
     * - 샘플 데이터의 중복 코드를 줄이기 위한 메서드입니다.
     * 
     * @param id 샘플 데이터 ID
     * @param recTitle 추천 활동 제목
     * @param recDesc 추천 활동 설명
     * @param reviewTitle 후기 제목
     * @param reviewContent 후기 내용
     * @param userName 사용자 이름
     * @param stars 별점 (1-5)
     */
    private void createSampleReview(Long id, String recTitle, String recDesc, 
                                   String reviewTitle, String reviewContent, 
                                   String userName, int stars) {
        // 1. 추천 활동 가져오기 또는 생성하기
        Recommendation recommendation = null;
        
        // ID로 기존 추천 활동 찾기
        if (recommendationRepository.findById(id).isPresent()) {
            recommendation = recommendationRepository.findById(id).get();
        } else {
            // 없으면 새 추천 활동 생성
            Recommendation newRecommendation = Recommendation.builder()
                    .title(recTitle)
                    .description(recDesc)
                    .authorName(SYSTEM_AUTHOR)
                    .build();
            recommendation = recommendationRepository.save(newRecommendation);
        }
        
        // 2. 확정된 계획 가져오기 또는 생성하기
        ConfirmedPlan confirmedPlan = null;
        
        // ID로 기존 확정 계획 찾기
        if (confirmedPlanRepository.findById(id).isPresent()) {
            confirmedPlan = confirmedPlanRepository.findById(id).get();
        } else {
            // 없으면 새 확정 계획 생성
            ConfirmedPlan newPlan = ConfirmedPlan.builder()
                    .plannerName(userName)
                    .recommendation(recommendation)
                    .confirmedAt(LocalDateTime.now().minusDays(id))
                    .build();
            confirmedPlan = confirmedPlanRepository.save(newPlan);
        }
        
        // 3. 후기 생성
        Review review = Review.builder()
                .authorName(userName)
                .confirmedPlan(confirmedPlan)
                .title(reviewTitle)
                .content(reviewContent)
                .starRating(stars)
                .build();
        
        // 4. 후기 저장
        reviewRepository.save(review);
    }

    /**
     * 새 후기를 저장합니다.
     * 
     * @param requestDto 후기 정보가 담긴 DTO
     * @param authorName 작성자 이름
     * @return 저장된 후기의 ID
     * @throws IllegalArgumentException 확정 계획이 없거나 이미 후기가 작성된 경우
     */
    @Transactional
    public Long save(ReviewDto.RequestDto requestDto, String authorName) {
        // 작성자 이름이 없으면 기본값 사용
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = DEFAULT_AUTHOR;
        }
        
        // 1. 확정 계획 찾기
        ConfirmedPlan confirmedPlan = confirmedPlanRepository.findById(requestDto.getConfirmedPlanId())
                .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다."));
        
        // 2. 이미 후기가 작성되었는지 확인
        if (reviewRepository.existsByConfirmedPlan(confirmedPlan)) {
            throw new IllegalArgumentException("이미 후기가 작성된 확정 계획입니다.");
        }
        
        // 3. DTO를 엔티티로 변환 후 저장
        Review review = requestDto.toEntity(authorName, confirmedPlan);
        Review savedReview = reviewRepository.save(review);
        
        return savedReview.getId();
    }

    /**
     * 기존 후기를 수정합니다.
     * 
     * @param id 수정할 후기 ID
     * @param requestDto 수정할 내용이 담긴 DTO
     * @param authorName 작성자 이름 (권한 확인용)
     * @throws IllegalArgumentException 해당 ID의 후기가 없는 경우
     */
    @Transactional
    public void update(Long id, ReviewDto.RequestDto requestDto, String authorName) {
        // 1. ID로 후기 찾기
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. ID: " + id));
        
        // 2. 후기 내용 업데이트
        review.update(
            requestDto.getTitle(), 
            requestDto.getContent(), 
            requestDto.getImageUrl(), 
            requestDto.getStarRating()
        );
        
        // 트랜잭션이 끝나면 변경사항이 자동으로 저장됨
    }

    /**
     * 후기를 삭제합니다.
     * 삭제 전 연관된 모든 댓글과 좋아요도 함께 삭제합니다.
     * 
     * @param id 삭제할 후기 ID
     * @param authorName 작성자 이름 (권한 확인용)
     * @throws IllegalArgumentException 해당 ID의 후기가 없는 경우
     */
    @Transactional
    public void delete(Long id, String authorName) {
        // 1. ID로 후기 찾기
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. ID: " + id));
        
        // 2. 관련 데이터 삭제 (외래키 제약조건 오류 방지를 위해 순서 중요)
        // 2-1. 댓글 먼저 삭제
        commentRepository.deleteAllByReview(review);
        
        // 2-2. 좋아요 삭제
        likeRepository.deleteAllByReview(review);
        
        // 3. 후기 삭제
        reviewRepository.delete(review);
    }

    /**
     * ID로 후기 한 건을 조회합니다.
     * 
     * @param id 조회할 후기 ID
     * @param authorName 요청한 사용자 이름
     * @return 후기 상세 정보가 담긴 DTO
     * @throws IllegalArgumentException 해당 ID의 후기가 없는 경우
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (성능 최적화)
    public ReviewDto.ResponseDto findById(Long id, String authorName) {
        // 1. ID로 후기 찾기
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. ID: " + id));
        
        // 2. 좋아요 수 조회
        long likeCount = likeRepository.countByReview(review);
        
        // 3. 작성자 본인 여부 확인
        boolean isAuthor = authorName != null && authorName.equals(review.getAuthorName());
        
        // 4. 현재 사용자가 좋아요 했는지 확인
        boolean isLiked = false;
        if (authorName != null) {
            isLiked = likeRepository.existsByAuthorNameAndReview(authorName, review);
        }
        
        // 5. 엔티티를 DTO로 변환하여 반환
        return new ReviewDto.ResponseDto(review, likeCount, isAuthor, isLiked);
    }

    /**
     * 페이징된 후기 목록을 조회합니다.
     * 
     * @param pageable 페이징 정보
     * @return 페이징된 후기 목록 DTO
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto.ListResponseDto> findAllPaging(Pageable pageable) {
        // 페이징된 후기 목록 조회 (Spring Data JPA가 페이징 처리)
        Page<Review> reviewPage = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        // Page 객체의 map 메서드를 사용하여 각 엔티티를 DTO로 변환
        return reviewPage.map(review -> {
            // 각 후기의 좋아요 수 조회
            long likeCount = likeRepository.countByReview(review);
            
            // 각 후기의 댓글 수 조회
            long commentCount = commentRepository.findAllByReviewOrderByCreatedAtAsc(review).size();
            
            // 엔티티를 DTO로 변환
            return new ReviewDto.ListResponseDto(review, likeCount, commentCount);
        });
    }

    /**
     * 특정 작성자의 모든 후기를 조회합니다.
     * 
     * @param authorName 작성자 이름
     * @return 후기 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<ReviewDto.ListResponseDto> findAllByAuthorName(String authorName) {
        // 작성자 이름이 없으면 기본값 사용
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = DEFAULT_AUTHOR;
        }
        
        // 1. 작성자 이름으로 후기 목록 조회
        List<Review> reviewList = reviewRepository.findAllByAuthorNameOrderByCreatedAtDesc(authorName);
        
        // 2. 결과를 담을 DTO 리스트 생성
        List<ReviewDto.ListResponseDto> dtoList = new ArrayList<>();
        
        // 3. 각 후기를 DTO로 변환하여 리스트에 추가
        for (Review review : reviewList) {
            // 좋아요 수 조회
            long likeCount = likeRepository.countByReview(review);
            
            // 댓글 수 조회
            long commentCount = commentRepository.findAllByReviewOrderByCreatedAtAsc(review).size();
            
            // DTO 생성 후 리스트에 추가
            ReviewDto.ListResponseDto dto = new ReviewDto.ListResponseDto(review, likeCount, commentCount);
            dtoList.add(dto);
        }
        
        return dtoList;
    }
}