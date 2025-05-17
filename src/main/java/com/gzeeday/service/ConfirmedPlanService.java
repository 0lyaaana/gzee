package com.gzeeday.service;

import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.plan.ConfirmedPlanRepository;
import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.web.dto.plan.ConfirmedPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfirmedPlanService {

    private final ConfirmedPlanRepository confirmedPlanRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Long confirm(ConfirmedPlanDto.RequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Recommendation recommendation = recommendationRepository.findById(requestDto.getRecommendationId())
                .orElseThrow(() -> new IllegalArgumentException("해당 추천을 찾을 수 없습니다."));
        
        // 이미 확정한 추천인지 확인
        if (confirmedPlanRepository.existsByUserAndRecommendationId(user, recommendation.getId())) {
            throw new IllegalArgumentException("이미 확정한 추천입니다.");
        }
        
        ConfirmedPlan confirmedPlan = ConfirmedPlan.builder()
                .user(user)
                .recommendation(recommendation)
                .confirmedAt(LocalDateTime.now())
                .build();
        
        confirmedPlanRepository.save(confirmedPlan);
        
        return confirmedPlan.getId();
    }

    @Transactional(readOnly = true)
    public List<ConfirmedPlanDto.ResponseDto> findAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return confirmedPlanRepository.findAllByUserOrderByConfirmedAtDesc(user).stream()
                .map(confirmedPlan -> {
                    boolean hasReview = reviewRepository.existsByConfirmedPlan(confirmedPlan);
                    return new ConfirmedPlanDto.ResponseDto(confirmedPlan, hasReview);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConfirmedPlanDto.ResponseDto findById(Long id, Long userId) {
        // 비로그인 사용자를 위한 처리 (userId가 null이거나 기본값인 경우)
        if (userId == null || userId == 1L) {
            ConfirmedPlan confirmedPlan = confirmedPlanRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다."));
            
            boolean hasReview = reviewRepository.existsByConfirmedPlan(confirmedPlan);
            return new ConfirmedPlanDto.ResponseDto(confirmedPlan, hasReview);
        }
        
        // 로그인 사용자를 위한 기존 처리
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ConfirmedPlan confirmedPlan = confirmedPlanRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다."));
        
        boolean hasReview = reviewRepository.existsByConfirmedPlan(confirmedPlan);
        
        return new ConfirmedPlanDto.ResponseDto(confirmedPlan, hasReview);
    }
    
    @Transactional(readOnly = true)
    public ConfirmedPlanDto.ResponseDto findByIdForAnyone(Long id) {
        ConfirmedPlan confirmedPlan = confirmedPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 확정 계획을 찾을 수 없습니다."));
        
        boolean hasReview = reviewRepository.existsByConfirmedPlan(confirmedPlan);
        
        return new ConfirmedPlanDto.ResponseDto(confirmedPlan, hasReview);
    }
} 