package com.gzeeday.web.dto.recommendation;

import com.gzeeday.domain.recommendation.UserRecommendation;
import com.gzeeday.domain.recommendation.UserRecommendationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserRecommendationDto {

    @Getter
    @NoArgsConstructor
    public static class RequestDto {
        @NotEmpty(message = "제목은 필수 입력값입니다.")
        private String title;
        
        @NotEmpty(message = "내용은 필수 입력값입니다.")
        private String content;
        
        @NotEmpty(message = "추천 이유는 필수 입력값입니다.")
        private String reason;
        
        @Builder
        public RequestDto(String title, String content, String reason) {
            this.title = title;
            this.content = content;
            this.reason = reason;
        }
    }
    
    @Getter
    public static class ResponseDto {
        private Long id;
        private String title;
        private String content;
        private String reason;
        private String author;
        private UserRecommendationStatus status;
        private String statusText;
        private String createdAt;
        private boolean isAuthor;
        private boolean isPending;
        private boolean isApproved;
        private boolean isRejected;
        
        public ResponseDto(UserRecommendation entity, boolean isAuthor) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.content = entity.getContent();
            this.reason = entity.getReason();
            this.author = entity.getAuthor();
            this.status = entity.getStatus();
            this.statusText = getStatusText(entity.getStatus());
            this.createdAt = entity.getCreatedAt().format(formatter);
            this.isAuthor = isAuthor;
            this.isPending = entity.getStatus() == UserRecommendationStatus.PENDING;
            this.isApproved = entity.getStatus() == UserRecommendationStatus.APPROVED;
            this.isRejected = entity.getStatus() == UserRecommendationStatus.REJECTED;
        }
        
        private String getStatusText(UserRecommendationStatus status) {
            switch (status) {
                case PENDING:
                    return "대기 중";
                case APPROVED:
                    return "승인됨";
                case REJECTED:
                    return "기각됨";
                default:
                    return "";
            }
        }
    }
    
    @Getter
    public static class ListResponseDto {
        private Long id;
        private String title;
        private String author;
        private UserRecommendationStatus status;
        private String statusText;
        private String createdAt;
        private boolean isPending;
        private boolean isApproved;
        private boolean isRejected;
        
        public ListResponseDto(UserRecommendation entity) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.author = entity.getAuthor();
            this.status = entity.getStatus();
            this.statusText = getStatusText(entity.getStatus());
            this.createdAt = entity.getCreatedAt().format(formatter);
            this.isPending = entity.getStatus() == UserRecommendationStatus.PENDING;
            this.isApproved = entity.getStatus() == UserRecommendationStatus.APPROVED;
            this.isRejected = entity.getStatus() == UserRecommendationStatus.REJECTED;
        }
        
        private String getStatusText(UserRecommendationStatus status) {
            switch (status) {
                case PENDING:
                    return "대기 중";
                case APPROVED:
                    return "승인됨";
                case REJECTED:
                    return "기각됨";
                default:
                    return "";
            }
        }
    }
} 