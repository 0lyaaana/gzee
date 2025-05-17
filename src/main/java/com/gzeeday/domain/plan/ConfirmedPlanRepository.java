package com.gzeeday.domain.plan;

import com.gzeeday.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfirmedPlanRepository extends JpaRepository<ConfirmedPlan, Long> {
    List<ConfirmedPlan> findAllByUserOrderByConfirmedAtDesc(User user);
    Optional<ConfirmedPlan> findByIdAndUser(Long id, User user);
    boolean existsByUserAndRecommendationId(User user, Long recommendationId);
} 