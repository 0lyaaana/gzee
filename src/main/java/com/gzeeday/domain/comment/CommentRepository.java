package com.gzeeday.domain.comment;

import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByReviewOrderByCreatedAtAsc(Review review);
    List<Comment> findAllByUserOrderByCreatedAtDesc(User user);
    
    // 특정 리뷰에 속한 모든 댓글 삭제
    @Transactional
    void deleteAllByReview(Review review);
} 