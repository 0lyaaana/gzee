package com.gzeeday.domain.plan;

import com.gzeeday.domain.BaseTimeEntity;
import com.gzeeday.domain.recommendation.Recommendation;
import com.gzeeday.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class ConfirmedPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id")
    private Recommendation recommendation;

    @Column(nullable = false)
    private LocalDateTime confirmedAt;

    @Builder
    public ConfirmedPlan(User user, Recommendation recommendation, LocalDateTime confirmedAt) {
        this.user = user;
        this.recommendation = recommendation;
        this.confirmedAt = confirmedAt;
    }
} 