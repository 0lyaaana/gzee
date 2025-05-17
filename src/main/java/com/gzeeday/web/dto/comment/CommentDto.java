package com.gzeeday.web.dto.comment;

import com.gzeeday.domain.comment.Comment;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.format.DateTimeFormatter;

public class CommentDto {

    @Getter
    @NoArgsConstructor
    public static class RequestDto {
        @NotBlank(message = "댓글 내용을 입력해주세요")
        @Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다")
        private String content;

        @Builder
        public RequestDto(String content) {
            this.content = content;
        }

        public Comment toEntity(User user, Review review) {
            return Comment.builder()
                    .user(user)
                    .review(review)
                    .content(content)
                    .build();
        }
    }

    @Getter
    public static class ResponseDto {
        private Long id;
        private String content;
        private String author;
        private String createdAt;
        private boolean isEdited;
        private boolean isAuthor;

        @Builder
        public ResponseDto(Comment entity, boolean isAuthor) {
            this.id = entity.getId();
            this.content = entity.getContent();
            this.author = entity.getUser().getNickname();
            this.createdAt = entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            this.isEdited = entity.isEdited();
            this.isAuthor = isAuthor;
        }
    }
} 