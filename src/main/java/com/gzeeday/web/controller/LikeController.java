package com.gzeeday.web.controller;

import com.gzeeday.service.LikeService;
import com.gzeeday.web.dto.like.LikeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;
    
    // 모든 좋아요에서 사용할 기본 사용자명
    private static final String DEFAULT_LIKER_NAME = "사용자";

    @PostMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@PathVariable Long reviewId) {
        try {
            // 모든 사용자가 좋아요 가능하도록 수정
            LikeDto.ResponseDto responseDto = likeService.toggleLike(reviewId, DEFAULT_LIKER_NAME);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 