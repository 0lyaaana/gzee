package com.gzeeday.service;

import com.gzeeday.domain.comment.Comment;
import com.gzeeday.domain.comment.CommentRepository;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.web.dto.comment.CommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long save(Long reviewId, CommentDto.RequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다."));
        
        Comment comment = requestDto.toEntity(user, review);
        commentRepository.save(comment);
        
        return comment.getId();
    }

    @Transactional
    public Long update(Long id, CommentDto.RequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
        
        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 댓글을 수정할 권한이 없습니다.");
        }
        
        comment.update(requestDto.getContent());
        
        return id;
    }

    @Transactional
    public void delete(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
        
        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 댓글을 삭제할 권한이 없습니다.");
        }
        
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto.ResponseDto> findAllByReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다."));
        
        return commentRepository.findAllByReviewOrderByCreatedAtAsc(review).stream()
                .map(comment -> {
                    boolean isAuthor = userId != null && comment.getUser().getId().equals(userId);
                    return new CommentDto.ResponseDto(comment, isAuthor);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDto.ResponseDto> findAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return commentRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(comment -> new CommentDto.ResponseDto(comment, true))
                .collect(Collectors.toList());
    }
} 