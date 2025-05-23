package com.gzeeday.domain.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 추천 활동 리포지토리
 * - 추천 활동 데이터에 접근하는 인터페이스입니다.
 * - Spring Data JPA가 자동으로 구현체를 생성합니다.
 */
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    
    /**
     * 무작위로 추천 활동 한 건을 조회합니다.
     * - 데이터베이스의 모든 추천 활동 중 하나를 랜덤으로 선택합니다.
     * - H2 데이터베이스는 RAND() 함수를 지원합니다.
     * 
     * @return 랜덤으로 선택된 추천 활동
     */
    @Query(nativeQuery = true, value = "SELECT * FROM recommendation ORDER BY RAND() LIMIT 1")
    Recommendation findRandomRecommendation();
    
    /**
     * 모든 추천 활동을 생성일 기준 내림차순(최신순)으로 조회합니다.
     * - 메서드 이름 규칙에 따라 Spring Data JPA가 자동으로 쿼리를 생성합니다.
     * 
     * @return 생성일 기준 내림차순으로 정렬된 추천 활동 목록
     */
    List<Recommendation> findAllByOrderByCreatedAtDesc();
} 