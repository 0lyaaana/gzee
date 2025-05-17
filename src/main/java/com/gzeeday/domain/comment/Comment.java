package com.gzeeday.domain.comment;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private boolean isEdited;

    @Builder
    public Comment(User user, Review review, String content) {
        this.user = user;
        this.review = review;
        this.content = content;
        this.isEdited = false;
    }

    public void update(String content) {
        this.content = content;
        this.isEdited = true;
    }
} 