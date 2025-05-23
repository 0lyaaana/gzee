package com.gzeeday.domain.like;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.review.Review;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 좋아요 엔티티
 * - 후기에 대한 좋아요 정보를 저장합니다.
 * - 작성자 이름과 연결된 후기 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "likes")  // 'like'는 SQL 예약어이므로 테이블명을 'likes'로 지정
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String authorName;        // 좋아요 작성자 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;            // 좋아요가 달린 후기

    @Builder
    public Like(String authorName, Review review) {
        this.authorName = authorName;
        this.review = review;
    }
} 