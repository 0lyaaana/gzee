package com.gzeeday.domain.comment;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.review.Review;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 댓글 엔티티
 * - 후기에 작성된 댓글 정보를 저장합니다.
 * - 작성자 이름, 연결된 후기, 댓글 내용, 수정 여부 정보를 포함합니다.
 */
@Getter
@NoArgsConstructor
@Entity
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String authorName;        // 댓글 작성자 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;            // 댓글이 작성된 후기

    @Column(nullable = false, length = 1000)
    private String content;           // 댓글 내용

    @Column(nullable = false)
    private boolean isEdited;         // 수정 여부

    @Builder
    public Comment(String authorName, Review review, String content) {
        this.authorName = authorName;
        this.review = review;
        this.content = content;
        this.isEdited = false;
    }

    /**
     * 댓글 내용 업데이트
     * - 내용 변경 및 수정 여부 플래그 설정
     */
    public void update(String content) {
        this.content = content;
        this.isEdited = true;
    }
} 