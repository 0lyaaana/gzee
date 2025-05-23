package com.gzeeday.config;

import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 초기 데이터 로더
 * - 애플리케이션 시작 시 기본 추천 활동 데이터를 생성합니다.
 * - 데이터베이스가 비어있을 때만 실행됩니다.
 */
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final RecommendationRepository recommendationRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 초기 추천 데이터 생성 (아직 데이터가 없는 경우에만)
        if (recommendationRepository.count() == 0) {
            String defaultAuthor = "시스템 관리자";
            
            // 기본 추천 활동 목록 생성
            List<Recommendation> recommendations = Arrays.asList(
                Recommendation.builder()
                    .title("여의도 한강라면")
                    .description("여의도 한강가서 밤 9시에 한강라면 먹고 오기")
                    .authorName(defaultAuthor)
                    .build(),
                
                Recommendation.builder()
                    .title("강릉 서점 여행")
                    .description("강릉 서점에 가 책 한 권 산 다음 경포대 해변 가서 돗자리 깔고 책 읽기")
                    .authorName(defaultAuthor)
                    .build(),
                
                Recommendation.builder()
                    .title("버스 여행")
                    .description("버스정류장에 도착한 후 3번째로 온 버스 타고 창밖 구경하며 종점까지 가기")
                    .authorName(defaultAuthor)
                    .build(),
                
                Recommendation.builder()
                    .title("영화 속 장소 탐방")
                    .description("최근에 본 영화나 드라마에 나온 장소 찾아가기")
                    .authorName(defaultAuthor)
                    .build(),
                
                Recommendation.builder()
                    .title("등산 피크닉")
                    .description("김밥이나 맛있는 간식 싸 들고 해발 300m 이상인 산 등산하기")
                    .authorName(defaultAuthor)
                    .build()
            );
            
            // 모든 추천 활동을 한 번에 저장
            recommendationRepository.saveAll(recommendations);
        }
    }
} 