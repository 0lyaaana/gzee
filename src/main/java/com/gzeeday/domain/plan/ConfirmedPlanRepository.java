package com.gzeeday.domain.plan;

import com.gzeeday.domain.recommendation.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfirmedPlanRepository extends JpaRepository<ConfirmedPlan, Long> {
    List<ConfirmedPlan> findAllByPlannerNameOrderByConfirmedAtDesc(String plannerName);
    Optional<ConfirmedPlan> findByIdAndPlannerName(Long id, String plannerName);
    boolean existsByPlannerNameAndRecommendationId(String plannerName, Long recommendationId);
    
    // 추천 활동 ID로 확정 계획 검색
    List<ConfirmedPlan> findAllByRecommendationId(Long recommendationId);
} 