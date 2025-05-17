package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
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
    private final UserRepository userRepository;

    @PostMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@PathVariable Long reviewId, @LoginUser SessionUser user) {
        Long userId = 1L; // 기본 사용자 ID 설정 (비로그인 사용자용)
        
        try {
            // 비로그인 사용자도 좋아요 가능하도록 수정
            LikeDto.ResponseDto responseDto = likeService.toggleLike(reviewId, user != null ? user.getId() : userId);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 