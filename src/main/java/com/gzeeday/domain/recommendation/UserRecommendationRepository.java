package com.gzeeday.domain.recommendation;

import com.gzeeday.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, Long> {
    Page<UserRecommendation> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<UserRecommendation> findAllByStatusOrderByCreatedAtDesc(UserRecommendationStatus status);
    Page<UserRecommendation> findAllByStatusOrderByCreatedAtDesc(UserRecommendationStatus status, Pageable pageable);
    List<UserRecommendation> findAllByUserOrderByCreatedAtDesc(User user);
} 