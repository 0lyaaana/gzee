package com.gzeeday.domain.recommendation;

import com.gzeeday.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 추천 활동 엔티티
 * - 사용자에게 추천되는 활동 정보를 저장합니다.
 * - 활동 제목, 설명, 작성자 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@Entity
public class Recommendation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;           // 활동 제목

    @Column(nullable = false, length = 1000)
    private String description;     // 활동 설명

    @Column
    private String authorName;      // 작성자 이름

    @Builder
    public Recommendation(String title, String description, String authorName) {
        this.title = title;
        this.description = description;
        this.authorName = authorName;
    }

    /**
     * 추천 활동 정보 업데이트
     */
    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }
} 