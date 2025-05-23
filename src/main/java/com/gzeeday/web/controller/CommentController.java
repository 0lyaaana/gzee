package com.gzeeday.web.controller;

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
    
    // 모든 댓글에서 사용할 기본 사용자명
    private static final String DEFAULT_USER_NAME = "사용자";

    @GetMapping("/{reviewId}")
    @ResponseBody
    public List<CommentDto.ResponseDto> getComments(@PathVariable Long reviewId) {
        return commentService.findAllByReview(reviewId, DEFAULT_USER_NAME);
    }

    @PostMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> addComment(@PathVariable Long reviewId, 
                                       @Valid @RequestBody CommentDto.RequestDto requestDto) {
        try {
            Long commentId = commentService.save(reviewId, requestDto, DEFAULT_USER_NAME);
            return ResponseEntity.ok(commentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateComment(@PathVariable Long id, 
                                          @Valid @RequestBody CommentDto.RequestDto requestDto) {
        try {
            Long commentId = commentService.update(id, requestDto, DEFAULT_USER_NAME);
            return ResponseEntity.ok(commentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            commentService.delete(id, DEFAULT_USER_NAME);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 