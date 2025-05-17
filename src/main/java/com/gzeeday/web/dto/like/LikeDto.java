package com.gzeeday.web.dto.like;

import lombok.Builder;
import lombok.Getter;

public class LikeDto {

    @Getter
    public static class ResponseDto {
        private Long reviewId;
        private long likeCount;
        private boolean isLiked;

        @Builder
        public ResponseDto(Long reviewId, long likeCount, boolean isLiked) {
            this.reviewId = reviewId;
            this.likeCount = likeCount;
            this.isLiked = isLiked;
        }
    }
} 