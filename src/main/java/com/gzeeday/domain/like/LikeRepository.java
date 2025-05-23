package com.gzeeday.domain.like;

import com.gzeeday.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByAuthorNameAndReview(String authorName, Review review);
    List<Like> findAllByReview(Review review);
    List<Like> findAllByAuthorNameOrderByCreatedAtDesc(String authorName);
    boolean existsByAuthorNameAndReview(String authorName, Review review);
    void deleteByAuthorNameAndReview(String authorName, Review review);
    long countByReview(Review review);
    
    // 특정 리뷰에 속한 모든 좋아요 삭제
    @Transactional
    void deleteAllByReview(Review review);
} 