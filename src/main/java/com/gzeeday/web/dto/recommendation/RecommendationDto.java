package com.gzeeday.web.dto.recommendation;

import com.gzeeday.domain.recommendation.Recommendation;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.format.DateTimeFormatter;

public class RecommendationDto {

    @Getter
    @NoArgsConstructor
    public static class RequestDto {
        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
        private String title;

        @NotBlank(message = "설명을 입력해주세요")
        @Size(max = 1000, message = "설명은 최대 1000자까지 입력 가능합니다")
        private String description;

        @Builder
        public RequestDto(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public Recommendation toEntity(String authorName) {
            return Recommendation.builder()
                    .title(title)
                    .description(description)
                    .authorName(authorName)
                    .build();
        }
    }

    @Getter
    public static class ResponseDto {
        private Long id;
        private String title;
        private String description;
        private String authorName;
        private String createdAt;

        @Builder
        public ResponseDto(Recommendation entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.description = entity.getDescription();
            this.authorName = entity.getAuthorName() != null ? entity.getAuthorName() : "관리자";
            this.createdAt = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }

    @Getter
    public static class ListResponseDto {
        private Long id;
        private String title;
        private String description;
        private String authorName;
        private String createdAt;

        @Builder
        public ListResponseDto(Recommendation entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.description = entity.getDescription();
            this.authorName = entity.getAuthorName() != null ? entity.getAuthorName() : "관리자";
            this.createdAt = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
} 