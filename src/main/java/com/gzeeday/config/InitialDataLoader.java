package com.gzeeday.config;

import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.recommendation.RecommendationRepository;
import com.gzeeday.domain.user.Role;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 관리자 계정 생성
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .name("관리자")
                    .nickname("관리자")
                    .username("admin")
                    .password(passwordEncoder.encode("admin1234"))
                    .email("admin@gzeeday.com")
                    .phone("010-1234-5678")
                    .role(Role.ADMIN)
                    .emailVerified(true)
                    .build();
            
            userRepository.save(admin);
            
            // 테스트 사용자 계정 생성
            User testUser = User.builder()
                    .name("테스트")
                    .nickname("테스터")
                    .username("test")
                    .password(passwordEncoder.encode("test1234"))
                    .email("test@gzeeday.com")
                    .phone("010-9876-5432")
                    .role(Role.USER)
                    .emailVerified(true)
                    .build();
            
            userRepository.save(testUser);
            
            // 초기 추천 데이터 생성
            Recommendation recommendation1 = Recommendation.builder()
                    .title("여의도 한강라면")
                    .description("여의도 한강가서 밤 9시에 한강라면 먹고 오기")
                    .createdBy(admin)
                    .build();
            
            Recommendation recommendation2 = Recommendation.builder()
                    .title("강릉 서점 여행")
                    .description("강릉 서점에 가 책 한 권 산 다음 경포대 해변 가서 돗자리 깔고 책 읽기")
                    .createdBy(admin)
                    .build();
            
            Recommendation recommendation3 = Recommendation.builder()
                    .title("버스 여행")
                    .description("버스정류장에 도착한 후 3번째로 온 법스 타고 창밖 구경하며 종점까지 가기")
                    .createdBy(admin)
                    .build();
            
            Recommendation recommendation4 = Recommendation.builder()
                    .title("영화 속 장소 탐방")
                    .description("최근에 본 영화나 드라마에 나온 장소 찾아가기")
                    .createdBy(admin)
                    .build();
            
            Recommendation recommendation5 = Recommendation.builder()
                    .title("등산 피크닉")
                    .description("김밥이나 맛있는 간식 싸 들고 해발 300m 이상인 산 등산하기")
                    .createdBy(admin)
                    .build();
            
            recommendationRepository.save(recommendation1);
            recommendationRepository.save(recommendation2);
            recommendationRepository.save(recommendation3);
            recommendationRepository.save(recommendation4);
            recommendationRepository.save(recommendation5);
        }
    }
} 