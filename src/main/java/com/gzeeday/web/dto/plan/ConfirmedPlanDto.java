package com.gzeeday.web.dto.plan;

import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.recommendation.Recommendation;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfirmedPlanDto {

    @Getter
    @NoArgsConstructor
    public static class RequestDto {
        private Long recommendationId;

        @Builder
        public RequestDto(Long recommendationId) {
            this.recommendationId = recommendationId;
        }

        public ConfirmedPlan toEntity(String plannerName, Recommendation recommendation) {
            return ConfirmedPlan.builder()
                    .plannerName(plannerName)
                    .recommendation(recommendation)
                    .confirmedAt(LocalDateTime.now())
                    .build();
        }
    }

    @Getter
    public static class ResponseDto {
        private Long id;
        private Long recommendationId;
        private String title;
        private String description;
        private String plannerName;
        private String confirmedAt;
        private boolean hasReview;

        @Builder
        public ResponseDto(ConfirmedPlan entity, boolean hasReview) {
            this.id = entity.getId();
            this.recommendationId = entity.getRecommendation().getId();
            this.title = entity.getRecommendation().getTitle();
            this.description = entity.getRecommendation().getDescription();
            this.plannerName = entity.getPlannerName();
            this.confirmedAt = entity.getConfirmedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            this.hasReview = hasReview;
        }
    }
} 