package com.gzeeday.web.dto.review;

import com.gzeeday.domain.plan.ConfirmedPlan;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.format.DateTimeFormatter;

public class ReviewDto {

    @Getter
    @NoArgsConstructor
    public static class RequestDto {
        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
        private String title;

        @NotBlank(message = "내용을 입력해주세요")
        @Size(max = 2000, message = "내용은 최대 2000자까지 입력 가능합니다")
        private String content;

        private String imageUrl;

        @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다")
        @Max(value = 5, message = "별점은 최대 5점까지 가능합니다")
        private int starRating;

        private Long confirmedPlanId;

        @Builder
        public RequestDto(String title, String content, String imageUrl, int starRating, Long confirmedPlanId) {
            this.title = title;
            this.content = content;
            this.imageUrl = imageUrl;
            this.starRating = starRating;
            this.confirmedPlanId = confirmedPlanId;
        }

        public Review toEntity(User user, ConfirmedPlan confirmedPlan) {
            return Review.builder()
                    .user(user)
                    .confirmedPlan(confirmedPlan)
                    .title(title)
                    .content(content)
                    .imageUrl(imageUrl)
                    .starRating(starRating)
                    .build();
        }
    }

    @Getter
    public static class ResponseDto {
        private Long id;
        private String title;
        private String content;
        private String imageUrl;
        private int starRating;
        private String recommendationTitle;
        private String author;
        private String createdAt;
        private long likeCount;
        private boolean isAuthor;
        private boolean isLiked;

        @Builder
        public ResponseDto(Review entity, long likeCount, boolean isAuthor, boolean isLiked) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.content = entity.getContent();
            this.imageUrl = entity.getImageUrl();
            this.starRating = entity.getStarRating();
            this.recommendationTitle = entity.getConfirmedPlan().getRecommendation().getTitle();
            this.author = entity.getUser().getNickname();
            this.createdAt = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            this.likeCount = likeCount;
            this.isAuthor = isAuthor;
            this.isLiked = isLiked;
        }
        
        public boolean isAuthor() {
            return isAuthor;
        }
    }

    @Getter
    public static class ListResponseDto {
        private Long id;
        private String title;
        private String recommendationTitle;
        private String author;
        private int starRating;
        private String createdAt;
        private long likeCount;
        private long commentCount;

        @Builder
        public ListResponseDto(Review entity, long likeCount, long commentCount) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.recommendationTitle = entity.getConfirmedPlan().getRecommendation().getTitle();
            this.author = entity.getUser().getNickname();
            this.starRating = entity.getStarRating();
            this.createdAt = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.likeCount = likeCount;
            this.commentCount = commentCount;
        }
    }
} 