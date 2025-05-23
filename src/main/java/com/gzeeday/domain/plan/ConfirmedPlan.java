package com.gzeeday.domain.plan;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.review.Review;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 확정된 활동 계획 엔티티
 * - 사용자가 추천 활동 중에서 확정한 계획 정보를 저장합니다.
 * - 계획자 이름, 선택한 추천 활동, 확정 일시 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@Entity
public class ConfirmedPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String plannerName;       // 계획을 확정한 사용자 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id")
    private Recommendation recommendation;  // 확정한 추천 활동

    @Column(nullable = false)
    private LocalDateTime confirmedAt;      // 확정 일시
    
    @OneToOne(mappedBy = "confirmedPlan", fetch = FetchType.LAZY)
    private Review review;                  // 작성된 후기

    @Builder
    public ConfirmedPlan(String plannerName, Recommendation recommendation, LocalDateTime confirmedAt) {
        this.plannerName = plannerName;
        this.recommendation = recommendation;
        this.confirmedAt = confirmedAt;
    }
    
    /**
     * 후기가 작성되었는지 확인하는 메서드
     * @return 후기 작성 여부
     */
    public boolean hasReview() {
        return this.review != null;
    }
} 