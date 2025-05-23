package com.gzeeday.service;

import com.gzeeday.domain.comment.Comment;
import com.gzeeday.domain.comment.CommentRepository;
import com.gzeeday.domain.review.Review;
import com.gzeeday.domain.review.ReviewRepository;
import com.gzeeday.web.dto.comment.CommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 서비스
 * - 후기에 작성된 댓글 관련 기능을 처리하는 서비스 클래스입니다.
 * - 댓글 작성, 수정, 삭제, 조회 기능을 제공합니다.
 */
@Service // 스프링이 서비스 계층으로 인식하도록 하는 어노테이션
@RequiredArgsConstructor // final 필드를 초기화하는 생성자를 자동으로 생성
public class CommentService {

    // 필요한 Repository 객체들을 주입받음
    private final CommentRepository commentRepository; // 댓글 데이터 접근
    private final ReviewRepository reviewRepository; // 후기 데이터 접근
    
    // 자주 사용되는 상수 정의
    private static final String DEFAULT_AUTHOR = "익명"; // 작성자가 없을 때 사용하는 기본값

    /**
     * 새로운 댓글을 저장합니다.
     * 
     * @param reviewId 댓글을 달 후기 ID
     * @param requestDto 댓글 정보가 담긴 DTO
     * @param authorName 작성자 이름
     * @return 저장된 댓글의 ID
     * @throws IllegalArgumentException 해당 ID의 후기가 없는 경우
     */
    @Transactional
    public Long save(Long reviewId, CommentDto.RequestDto requestDto, String authorName) {
        // 작성자 이름이 없으면 기본값 사용
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = DEFAULT_AUTHOR;
        }
        
        // 1. 후기 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. ID: " + reviewId));
        
        // 2. DTO를 엔티티로 변환
        Comment comment = requestDto.toEntity(authorName, review);
        
        // 3. 댓글 저장
        Comment savedComment = commentRepository.save(comment);
        
        // 4. 저장된 댓글의 ID 반환
        return savedComment.getId();
    }

    /**
     * 기존 댓글을 수정합니다.
     * 
     * @param id 수정할 댓글 ID
     * @param requestDto 수정할 내용이 담긴 DTO
     * @param authorName 작성자 이름 (권한 확인용)
     * @return 수정된 댓글의 ID
     * @throws IllegalArgumentException 해당 ID의 댓글이 없는 경우
     */
    @Transactional
    public Long update(Long id, CommentDto.RequestDto requestDto, String authorName) {
        // 1. ID로 댓글 찾기
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. ID: " + id));
        
        // 2. 댓글 내용 업데이트 (수정 여부도 자동으로 true로 설정됨)
        comment.update(requestDto.getContent());
        
        // 3. 수정된 댓글의 ID 반환
        return id;
    }

    /**
     * 댓글을 삭제합니다.
     * 
     * @param id 삭제할 댓글 ID
     * @param authorName 작성자 이름 (권한 확인용)
     * @throws IllegalArgumentException 해당 ID의 댓글이 없는 경우
     */
    @Transactional
    public void delete(Long id, String authorName) {
        // 1. ID로 댓글 찾기
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. ID: " + id));
        
        // 2. 댓글 삭제
        commentRepository.delete(comment);
    }

    /**
     * 특정 후기에 달린 모든 댓글을 조회합니다.
     * 
     * @param reviewId 후기 ID
     * @param authorName 조회 요청자 이름
     * @return 댓글 목록 DTO
     * @throws IllegalArgumentException 해당 ID의 후기가 없는 경우
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 (성능 최적화)
    public List<CommentDto.ResponseDto> findAllByReview(Long reviewId, String authorName) {
        // 1. 후기 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후기를 찾을 수 없습니다. ID: " + reviewId));
        
        // 2. 후기에 달린 모든 댓글 조회 (생성일 기준 오름차순)
        List<Comment> comments = commentRepository.findAllByReviewOrderByCreatedAtAsc(review);
        
        // 3. 결과를 담을 DTO 리스트 생성
        List<CommentDto.ResponseDto> dtoList = new ArrayList<>();
        
        // 4. 각 댓글을 DTO로 변환하여 리스트에 추가
        for (Comment comment : comments) {
            // DTO 생성 후 리스트에 추가 (true는 편집 가능 여부를 의미)
            CommentDto.ResponseDto dto = new CommentDto.ResponseDto(comment, true);
            dtoList.add(dto);
        }
        
        return dtoList;
    }

    /**
     * 특정 사용자가 작성한 모든 댓글을 조회합니다.
     * 
     * @param authorName 작성자 이름
     * @return 댓글 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<CommentDto.ResponseDto> findAllByAuthorName(String authorName) {
        // 작성자 이름이 없으면 기본값 사용
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = DEFAULT_AUTHOR;
        }
        
        // 1. 작성자 이름으로 댓글 목록 조회 (생성일 기준 내림차순)
        List<Comment> comments = commentRepository.findAllByAuthorNameOrderByCreatedAtDesc(authorName);
        
        // 2. 결과를 담을 DTO 리스트 생성
        List<CommentDto.ResponseDto> dtoList = new ArrayList<>();
        
        // 3. 각 댓글을 DTO로 변환하여 리스트에 추가
        for (Comment comment : comments) {
            // DTO 생성 후 리스트에 추가 (true는 편집 가능 여부를 의미)
            CommentDto.ResponseDto dto = new CommentDto.ResponseDto(comment, true);
            dtoList.add(dto);
        }
        
        return dtoList;
    }
} 