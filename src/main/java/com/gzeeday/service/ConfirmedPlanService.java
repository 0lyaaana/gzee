package com.gzeeday.service;

import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.plan.ConfirmedPlanRepository;
import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.web.dto.plan.ConfirmedPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 확정 계획 서비스
 * - 사용자가 추천 활동을 확정하여 계획으로 만드는 기능을 처리하는 서비스 클래스입니다.
 * - 계획 확정, 조회 기능을 제공합니다.
 */
@Service // 스프링이 서비스 계층으로 인식하도록 하는 어노테이션
@RequiredArgsConstructor // final 필드를 초기화하는 생성자를 자동으로 생성
public class ConfirmedPlanService {

    // 필요한 Repository 객체들을 주입받음
    private final ConfirmedPlanRepository confirmedPlanRepository; // 확정 계획 데이터 접근
    private final RecommendationRepository recommendationRepository; // 추천 활동 데이터 접근
    private final ReviewRepository reviewRepository; // 후기 데이터 접근
    
    // 자주 사용되는 상수 정의
    private static final String DEFAULT_PLANNER = "익명"; // 계획자가 없을 때 사용하는 기본값

    /**
     * 추천 활동을 확정 계획으로 생성합니다.
     * 
     * @param requestDto 확정 요청 정보가 담긴 DTO
     * @param plannerName 계획자 이름
     * @return 저장된 확정 계획의 ID
     * @throws IllegalArgumentException 추천 활동이 없거나 이미 확정한 경우
     */
    @Transactional
    public Long confirm(ConfirmedPlanDto.RequestDto requestDto, String plannerName) {
        // 계획자 이름이 없으면 기본값 사용
        if (plannerName == null || plannerName.trim().isEmpty()) {
            plannerName = DEFAULT_PLANNER;
        }
        
        // 1. 추천 활동 찾기
        Recommendation recommendation = recommendationRepository.findById(requestDto.getRecommendationId())
                .orElseThrow(() -> new IllegalArgumentException("해당 추천을 찾을 수 없습니다. ID: " + requestDto.getRecommendationId()));
        
        // 2. 이미 확정한 추천인지 확인
        if (confirmedPlanRepository.existsByPlannerNameAndRecommendationId(plannerName, recommendation.getId())) {
            throw new IllegalArgumentException("이미 확정한 추천입니다.");
        }
        
        // 3. 확정 계획 엔티티 생성
        ConfirmedPlan confirmedPlan = ConfirmedPlan.builder()
                .plannerName(plannerName)
                .recommendation(recommendation)
                .confirmedAt(LocalDateTime.now()) // 현재 시간으로 확정 시간 설정
                .build();
        
        // 4. 확정 계획 저장
        ConfirmedPlan savedPlan = confirmedPlanRepository.save(confirmedPlan);
        
        // 5. 저장된 확정 계획의 ID 반환
        return savedPlan.getId();
    }

    /**
     * 특정 사용자의 모든 확정 계획을 조회합니다.
     * 
     * @param plannerName 계획자 이름
     * @return 확정 계획 목록 DTO
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (성능 최적화)
    public List<ConfirmedPlanDto.ResponseDto> findAllByPlannerName(String plannerName) {
        // 계획자 이름이 없으면 기본값 사용
        if (plannerName == null || plannerName.trim().isEmpty()) {
            plannerName = DEFAULT_PLANNER;
        }
        
        // 1. 계획자 이름으로 확정 계획 목록 조회 (확정 시간 기준 내림차순)
        List<ConfirmedPlan> plans = confirmedPlanRepository.findAllByPlannerNameOrderByConfirmedAtDesc(plannerName);
        
        // 2. 결과를 담을 DTO 리스트 생성
        List<ConfirmedPlanDto.ResponseDto> dtoList = new ArrayList<>();
        
        // 3. 각 확정 계획을 DTO로 변환하여 리스트에 추가
        for (ConfirmedPlan confirmedPlan : plans) {
            // 후기 작성 여부 확인
            boolean hasReview = reviewRepository.existsByConfirmedPlan(confirmedPlan);
            
            // DTO 생성 후 리스트에 추가
            ConfirmedPlanDto.ResponseDto dto = new ConfirmedPlanDto.ResponseDto(confirmedPlan, hasReview);
            dtoList.add(dto);
        }
        
        return dtoList;
    }

    /**
     * 특정 사용자의 확정 계획 한 건을 조회합니다.
     * 
     * @param id 확정 계획 ID
     * @param plannerName 계획자 이름 (권한 확인용)
     * @return 확정 계획 정보 DTO
     * @throws IllegalArgumentException 해당 ID의 확정 계획이 없는 경우
     */
    @Transactional(readOnly = true)
    public ConfirmedPlanDto.ResponseDto findById(Long id, String plannerName) {
        // 계획자 이름이 없으면 누구나 볼 수 있는 메서드 호출
        if (plannerName == null || plannerName.trim().isEmpty()) {
            return findByIdForAnyone(id);
        }
        
        // 1. ID와 계획자 이름으로 확정 계획 찾기
        ConfirmedPlan confirmedPlan = confirmedPlanRepository.findByIdAndPlannerName(id, plannerName)
                .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다. ID: " + id));
        
        // 2. 후기 작성 여부 확인
        boolean hasReview = reviewRepository.existsByConfirmedPlan(confirmedPlan);
        
        // 3. 엔티티를 DTO로 변환하여 반환
        return new ConfirmedPlanDto.ResponseDto(confirmedPlan, hasReview);
    }
    
    /**
     * 모든 사용자가 접근 가능한 확정 계획 조회 메서드
     * 
     * @param id 확정 계획 ID
     * @return 확정 계획 정보 DTO
     * @throws IllegalArgumentException 해당 ID의 확정 계획이 없는 경우
     */
    @Transactional(readOnly = true)
    public ConfirmedPlanDto.ResponseDto findByIdForAnyone(Long id) {
        // 1. ID로 확정 계획 찾기
        ConfirmedPlan confirmedPlan = confirmedPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다. ID: " + id));
        
        // 2. 후기 작성 여부 확인
        boolean hasReview = reviewRepository.existsByConfirmedPlan(confirmedPlan);
        
        // 3. 엔티티를 DTO로 변환하여 반환
        return new ConfirmedPlanDto.ResponseDto(confirmedPlan, hasReview);
    }
} 