package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.service.CommentService;
import com.gzeeday.web.dto.comment.CommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{reviewId}")
    @ResponseBody
    public List<CommentDto.ResponseDto> getComments(@PathVariable Long reviewId, @LoginUser SessionUser user) {
        Long userId = user != null ? user.getId() : null;
        return commentService.findAllByReview(reviewId, userId);
    }

    @PostMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> addComment(@PathVariable Long reviewId, 
                                       @Valid @RequestBody CommentDto.RequestDto requestDto, 
                                       @LoginUser SessionUser user) {
        Long userId = 1L; // 기본 사용자 ID 설정
        
        try {
            Long commentId = commentService.save(reviewId, requestDto, userId);
            return ResponseEntity.ok(commentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateComment(@PathVariable Long id, 
                                          @Valid @RequestBody CommentDto.RequestDto requestDto, 
                                          @LoginUser SessionUser user) {
        Long userId = 1L; // 기본 사용자 ID 설정
        
        try {
            Long commentId = commentService.update(id, requestDto, userId);
            return ResponseEntity.ok(commentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable Long id, @LoginUser SessionUser user) {
        Long userId = 1L; // 기본 사용자 ID 설정
        
        try {
            commentService.delete(id, user != null ? user.getId() : userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 