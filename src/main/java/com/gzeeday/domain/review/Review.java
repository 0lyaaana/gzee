package com.gzeeday.domain.review;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.plan.ConfirmedPlan;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 활동 후기 엔티티
 * - 사용자가 확정 계획 완료 후 작성한 후기 정보를 저장합니다.
 * - 작성자, 제목, 내용, 별점 등의 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@Entity
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String authorName;        // 후기 작성자 이름

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_plan_id")
    private ConfirmedPlan confirmedPlan;  // 이 후기가 작성된 확정 계획

    @Column(nullable = false)
    private String title;              // 후기 제목

    @Column(nullable = false, length = 2000)
    private String content;            // 후기 내용

    @Column
    private String imageUrl;           // 이미지 URL (선택사항)

    @Column(nullable = false)
    private int starRating;            // 별점 (1-5)

    @Builder
    public Review(String authorName, ConfirmedPlan confirmedPlan, String title, String content, String imageUrl, int starRating) {
        this.authorName = authorName;
        this.confirmedPlan = confirmedPlan;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.starRating = starRating;
    }

    /**
     * 후기 정보 업데이트
     */
    public void update(String title, String content, String imageUrl, int starRating) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.starRating = starRating;
    }
} 