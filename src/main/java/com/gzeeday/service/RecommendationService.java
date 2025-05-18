package com.gzeeday.service;

import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.web.dto.recommendation.RecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long save(RecommendationDto.RequestDto requestDto, Long userId) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElse(null);
        }
        
        Recommendation recommendation = requestDto.toEntity(user);
        recommendationRepository.save(recommendation);
        
        return recommendation.getId();
    }

    @Transactional
    public void update(Long id, RecommendationDto.RequestDto requestDto) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 추천을 찾을 수 없습니다."));
        
        recommendation.update(requestDto.getTitle(), requestDto.getDescription());
    }

    @Transactional
    public void delete(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 추천을 찾을 수 없습니다."));
        
        recommendationRepository.delete(recommendation);
    }

    @Transactional(readOnly = true)
    public RecommendationDto.ResponseDto findById(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 추천을 찾을 수 없습니다."));
        
        return new RecommendationDto.ResponseDto(recommendation);
    }

    @Transactional(readOnly = true)
    public List<RecommendationDto.ListResponseDto> findAll() {
        return recommendationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RecommendationDto.ListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecommendationDto.ResponseDto findRandom() {
        Recommendation recommendation = recommendationRepository.findRandomRecommendation();
        
        if (recommendation == null) {
            throw new IllegalArgumentException("추천 활동이 없습니다.");
        }
        
        return new RecommendationDto.ResponseDto(recommendation);
    }
} 