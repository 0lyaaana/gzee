package com.gzeeday.domain.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    @Query(value = "SELECT * FROM recommendation ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Recommendation findRandomRecommendation();
    
    List<Recommendation> findAllByOrderByCreatedAtDesc();
} 