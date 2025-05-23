package com.gzeeday.service;

import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.plan.ConfirmedPlanRepository;
import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.web.dto.recommendation.RecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 추천 활동 서비스
 * - 사용자에게 제공되는 활동 추천 관련 기능을 담당합니다.
 * - 추천 목록 조회, 등록, 수정, 삭제 기능을 제공합니다.
 * - 서비스 계층은 컨트롤러와 데이터 접근 계층(Repository) 사이에서 비즈니스 로직을 처리합니다.
 */
@Service // 이 클래스가 Spring의 서비스 계층 컴포넌트임을 나타냅니다.
@RequiredArgsConstructor // 생성자 주입을 자동으로 처리합니다.
public class RecommendationService {

    // 필요한 Repository들을 주입받습니다.
    private final RecommendationRepository recommendationRepository; // 추천 활동 데이터 접근 객체
    private final ConfirmedPlanRepository confirmedPlanRepository; // 확정 계획 데이터 접근 객체
    
    // 자주 사용되는 상수들을 정의합니다.
    private static final String DEFAULT_AUTHOR = "익명"; // 작성자가 없을 때 사용할 기본값
    private static final String SYSTEM_AUTHOR = "시스템 관리자"; // 시스템이 생성한 데이터의 작성자

    /**
     * 애플리케이션 시작 시 초기 샘플 데이터를 생성합니다.
     * - @PostConstruct: 스프링 빈 초기화 후 자동으로 호출됩니다.
     * - @Transactional: 이 메서드를 하나의 트랜잭션으로 처리합니다(모두 성공하거나 모두 실패).
     */
    @PostConstruct
    @Transactional
    public void initSampleData() {
        // 추천 데이터가 없는 경우에만 샘플 데이터 추가
        if (recommendationRepository.count() == 0) {
            // 샘플 추천 활동 데이터 배열 (제목, 설명)
            List<String[]> recommendationData = Arrays.asList(
                new String[]{"여의도 한강라면", "여의도 한강가서 밤 9시에 한강라면 먹고 오기"},
                new String[]{"강릉 서점 여행", "강릉 서점에 가 책 한 권 산 다음 경포대 해변 가서 돗자리 깔고 책 읽기"},
                new String[]{"버스 여행", "버스정류장에 도착한 후 3번째로 온 버스 타고 창밖 구경하며 종점까지 가기"},
                new String[]{"영화 속 장소 탐방", "최근에 본 영화나 드라마에 나온 장소 찾아가기"},
                new String[]{"등산 피크닉", "김밥이나 맛있는 간식 싸 들고 해발 300m 이상인 산 등산하기"},
                new String[]{"도서관 책 고르기", "지역 도서관에 가서 평소 읽지 않던 장르의 책 세 권 빌려오기"},
                new String[]{"골목 카페 투어", "동네 골목길에 있는 작은 카페 세 곳을 방문해 각각 다른 음료 마시기"},
                new String[]{"일출 보기", "가까운 산이나 강가에서 일출 보며 하루 시작하기"},
                new String[]{"시장 구경", "전통 시장에 가서 1만원으로 저녁 재료 사오기"},
                new String[]{"공원 산책", "근처 공원에서 30분 이상 산책하고 벤치에 앉아 사진 찍기"}
            );
            
            // 추천 활동 엔티티 목록 생성
            List<Recommendation> recommendations = new ArrayList<>();
            
            // 각 데이터로 Recommendation 객체 생성
            for (String[] data : recommendationData) {
                Recommendation recommendation = Recommendation.builder()
                    .title(data[0])           // 제목 설정
                    .description(data[1])     // 설명 설정
                    .authorName(SYSTEM_AUTHOR) // 작성자 설정
                    .build();
                
                // 목록에 추가
                recommendations.add(recommendation);
            }
            
            // 모든 추천 활동을 한 번에 저장
            recommendationRepository.saveAll(recommendations);
            
            System.out.println(recommendations.size() + "개의 샘플 추천 활동이 생성되었습니다.");
        }
    }

    /**
     * 새로운 추천 활동을 저장합니다.
     * 
     * @param requestDto 추천 활동 정보가 담긴 DTO 객체
     * @param authorName 작성자 이름
     * @return 저장된 추천 활동의 ID
     */
    @Transactional
    public Long save(RecommendationDto.RequestDto requestDto, String authorName) {
        // 작성자가 없으면 기본값 사용
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = DEFAULT_AUTHOR;
        }
        
        // DTO를 엔티티로 변환
        Recommendation recommendation = requestDto.toEntity(authorName);
        
        // 데이터베이스에 저장
        Recommendation savedRecommendation = recommendationRepository.save(recommendation);
        
        // 저장된 ID 반환
        return savedRecommendation.getId();
    }

    /**
     * 기존 추천 활동을 수정합니다.
     * 
     * @param id 수정할 추천 활동 ID
     * @param requestDto 수정할 내용이 담긴 DTO 객체
     * @throws IllegalArgumentException 해당 ID의 추천 활동이 없는 경우
     */
    @Transactional
    public void update(Long id, RecommendationDto.RequestDto requestDto) {
        // ID로 기존 추천 활동 조회
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 추천을 찾을 수 없습니다. ID: " + id));
        
        // 엔티티 내용 수정 (실제 DB 업데이트는 트랜잭션 종료 시 자동으로 수행됨)
        recommendation.update(requestDto.getTitle(), requestDto.getDescription());
    }

    /**
     * 추천 활동을 삭제합니다.
     * 연관된 확정 계획이 있다면 함께 삭제하지만, 후기가 있는 경우 삭제할 수 없습니다.
     * 
     * @param id 삭제할 추천 활동 ID
     * @throws IllegalArgumentException 해당 ID의 추천 활동이 없거나, 관련 후기가 있는 경우
     */
    @Transactional
    public void delete(Long id) {
        // ID로 추천 활동 조회
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 추천을 찾을 수 없습니다. ID: " + id));
        
        // 이 추천 활동과 연결된 모든 확정 계획 조회
        List<ConfirmedPlan> confirmedPlans = confirmedPlanRepository.findAllByRecommendationId(id);
        
        // 각 확정 계획 검사 및 삭제
        for (ConfirmedPlan confirmedPlan : confirmedPlans) {
            // 만약 확정 계획에 연결된 후기가 있다면 삭제 불가
            if (confirmedPlan.hasReview()) {
                throw new IllegalArgumentException("이미 후기가 작성된 확정 계획이 있어 삭제할 수 없습니다.");
            }
            // 후기가 없는 경우 확정 계획 삭제
            confirmedPlanRepository.delete(confirmedPlan);
        }
        
        // 추천 활동 삭제
        recommendationRepository.delete(recommendation);
    }

    /**
     * ID로 단일 추천 활동을 조회합니다.
     * 
     * @param id 조회할 추천 활동 ID
     * @return 추천 활동 정보 DTO
     * @throws IllegalArgumentException 해당 ID의 추천 활동이 없는 경우
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (성능 최적화)
    public RecommendationDto.ResponseDto findById(Long id) {
        // ID로 추천 활동 조회
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 추천을 찾을 수 없습니다. ID: " + id));
        
        // 엔티티를 DTO로 변환하여 반환
        return new RecommendationDto.ResponseDto(recommendation);
    }

    /**
     * 모든 추천 활동을 조회합니다.
     * 
     * @return 추천 활동 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<RecommendationDto.ListResponseDto> findAll() {
        // 모든 추천 활동을 생성일 기준 내림차순으로 조회
        List<Recommendation> recommendationList = recommendationRepository.findAllByOrderByCreatedAtDesc();
        
        // 결과를 담을 DTO 리스트 생성
        List<RecommendationDto.ListResponseDto> dtoList = new ArrayList<>();
        
        // 각 엔티티를 DTO로 변환하여 리스트에 추가
        for (Recommendation recommendation : recommendationList) {
            dtoList.add(new RecommendationDto.ListResponseDto(recommendation));
        }
        
        return dtoList;
    }

    /**
     * 랜덤 추천 활동을 조회합니다.
     * 
     * @return 무작위로 선택된 추천 활동 DTO
     * @throws IllegalArgumentException 추천 활동이 하나도 없는 경우
     */
    @Transactional(readOnly = true)
    public RecommendationDto.ResponseDto findRandom() {
        // 랜덤 추천 활동 조회 (repository에 해당 메서드 구현 필요)
        Recommendation recommendation = recommendationRepository.findRandomRecommendation();
        
        // 추천 활동이 없는 경우 예외 발생
        if (recommendation == null) {
            throw new IllegalArgumentException("추천 활동이 없습니다.");
        }
        
        // 엔티티를 DTO로 변환하여 반환
        return new RecommendationDto.ResponseDto(recommendation);
    }
}