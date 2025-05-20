package com.gzeeday.service;

import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.domain.recommendation.UserRecommendation;
import com.gzeeday.domain.recommendation.UserRecommendationRepository;
import com.gzeeday.domain.recommendation.UserRecommendationStatus;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.web.dto.recommendation.RecommendationDto;
import com.gzeeday.web.dto.recommendation.UserRecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserRecommendationService {

    private final UserRecommendationRepository userRecommendationRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long save(UserRecommendationDto.RequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + userId));

        String authorName = user.getName() != null ? user.getName() : "익명 사용자";
        
        UserRecommendation userRecommendation = UserRecommendation.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .reason(requestDto.getReason())
                .user(user)
                .author(authorName)
                .build();

        return userRecommendationRepository.save(userRecommendation).getId();
    }

    @Transactional(readOnly = true)
    public Page<UserRecommendationDto.ListResponseDto> findAllPaging(Pageable pageable) {
        Page<UserRecommendation> entities = userRecommendationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return entities.map(UserRecommendationDto.ListResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<UserRecommendationDto.ListResponseDto> findAllByStatus(Pageable pageable, String statusStr) {
        UserRecommendationStatus status = UserRecommendationStatus.valueOf(statusStr);
        Page<UserRecommendation> entities = userRecommendationRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable);
        return entities.map(UserRecommendationDto.ListResponseDto::new);
    }

    @Transactional(readOnly = true)
    public UserRecommendationDto.ResponseDto findById(Long id, Long userId) {
        UserRecommendation userRecommendation = userRecommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 추천 활동이 존재하지 않습니다. id=" + id));

        boolean isAuthor = userId != null && userRecommendation.getUser() != null &&
                userRecommendation.getUser().getId().equals(userId);

        return new UserRecommendationDto.ResponseDto(userRecommendation, isAuthor);
    }

    @Transactional
    public void approve(Long id) {
        UserRecommendation userRecommendation = userRecommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 추천 활동이 존재하지 않습니다. id=" + id));

        // 승인 상태로 변경
        userRecommendation.approve();

        // 새로운 추천 활동으로 등록
        Recommendation recommendation = Recommendation.builder()
                .title(userRecommendation.getTitle())
                .description(userRecommendation.getContent())
                .build();

        recommendationRepository.save(recommendation);
    }

    @Transactional
    public void reject(Long id) {
        UserRecommendation userRecommendation = userRecommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 추천 활동이 존재하지 않습니다. id=" + id));

        // 거부 상태로 변경
        userRecommendation.reject();
    }

    @Transactional
    public void delete(Long id, Long userId) {
        UserRecommendation userRecommendation = userRecommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 추천 활동이 존재하지 않습니다. id=" + id));

        if (!userRecommendation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 추천 활동을 삭제할 권한이 없습니다.");
        }

        userRecommendationRepository.delete(userRecommendation);
    }

    @Transactional(readOnly = true)
    public List<UserRecommendationDto.ListResponseDto> findAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + userId));

        return userRecommendationRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(UserRecommendationDto.ListResponseDto::new)
                .collect(Collectors.toList());
    }
} 