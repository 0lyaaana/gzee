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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Recommendation recommendation = requestDto.toEntity(user);
        recommendationRepository.save(recommendation);
        
        return recommendation.getId();
    }

    @Transactional
    public Long update(Long id, RecommendationDto.RequestDto requestDto, Long userId) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 추천을 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (recommendation.getCreatedBy() != null && !recommendation.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 추천을 수정할 권한이 없습니다.");
        }
        
        recommendation.update(requestDto.getTitle(), requestDto.getDescription());
        
        return id;
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 추천을 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (recommendation.getCreatedBy() != null && !recommendation.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 추천을 삭제할 권한이 없습니다.");
        }
        
        recommendationRepository.delete(recommendation);
    }

    @Transactional(readOnly = true)
    public RecommendationDto.ResponseDto findById(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 추천을 찾을 수 없습니다."));
        
        return new RecommendationDto.ResponseDto(recommendation);
    }

    @Transactional(readOnly = true)
    public List<RecommendationDto.ResponseDto> findAll() {
        return recommendationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RecommendationDto.ResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecommendationDto.ResponseDto findRandom() {
        Recommendation recommendation = recommendationRepository.findRandomRecommendation();
        
        if (recommendation == null) {
            throw new IllegalArgumentException("추천 목록이 비어있습니다.");
        }
        
        return new RecommendationDto.ResponseDto(recommendation);
    }
} 