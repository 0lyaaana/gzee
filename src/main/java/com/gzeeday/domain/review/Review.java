package com.gzeeday.domain.review;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_plan_id")
    private ConfirmedPlan confirmedPlan;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private int starRating;

    @Builder
    public Review(User user, ConfirmedPlan confirmedPlan, String title, String content, String imageUrl, int starRating) {
        this.user = user;
        this.confirmedPlan = confirmedPlan;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.starRating = starRating;
    }

    public void update(String title, String content, String imageUrl, int starRating) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.starRating = starRating;
    }
} 