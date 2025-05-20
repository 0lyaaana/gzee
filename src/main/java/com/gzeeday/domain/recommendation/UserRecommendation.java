package com.gzeeday.domain.recommendation;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class UserRecommendation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRecommendationStatus status;

    @Builder
    public UserRecommendation(String title, String content, String reason, User user, String author) {
        this.title = title;
        this.content = content;
        this.reason = reason;
        this.user = user;
        this.author = author;
        this.status = UserRecommendationStatus.PENDING; // 기본 상태는 대기 중
    }

    public void approve() {
        this.status = UserRecommendationStatus.APPROVED;
    }

    public void reject() {
        this.status = UserRecommendationStatus.REJECTED;
    }

    public boolean isPending() {
        return this.status == UserRecommendationStatus.PENDING;
    }
} 