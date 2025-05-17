package com.gzeeday.domain.like;

import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndReview(User user, Review review);
    List<Like> findAllByReview(Review review);
    List<Like> findAllByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndReview(User user, Review review);
    void deleteByUserAndReview(User user, Review review);
    long countByReview(Review review);
} 