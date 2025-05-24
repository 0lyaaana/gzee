package com.gzeeday.web.controller;

import com.gzeeday.service.CommentService;
import com.gzeeday.service.ConfirmedPlanService;
import com.gzeeday.service.ReviewService;
import com.gzeeday.web.dto.comment.CommentDto;
import com.gzeeday.web.dto.plan.ConfirmedPlanDto;
import com.gzeeday.web.dto.review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final CommentService commentService;
    private final ConfirmedPlanService confirmedPlanService;
    
    // 모든 리뷰에서 사용할 기본 사용자명
    private static final String DEFAULT_AUTHOR_NAME = "사용자";

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<ReviewDto.ListResponseDto> reviews = reviewService.findAllPaging(pageable);

        model.addAttribute("reviews", reviews);
        
        // 페이지네이션 정보 추가
        int currentPage = reviews.getNumber();
        int totalPages = reviews.getTotalPages();
        
        // 이전 페이지와 다음 페이지 계산
        int prevPage = Math.max(0, currentPage - 1);
        int nextPage = Math.min(totalPages - 1, currentPage + 1);
        
        // 페이지 번호 목록 생성
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);
        
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
        
        // 페이지 번호 리스트 생성
        java.util.List<PageNumberDto> pageNumbers = new java.util.ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(new PageNumberDto(i, i == currentPage, i + 1));
        }
        model.addAttribute("pageNumbers", pageNumbers);
        
        return "review/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ReviewDto.ResponseDto review = reviewService.findById(id, DEFAULT_AUTHOR_NAME);
        List<CommentDto.ResponseDto> comments = commentService.findAllByReview(id, DEFAULT_AUTHOR_NAME);
        
        model.addAttribute("review", review);
        model.addAttribute("comments", comments);
        model.addAttribute("commentRequest", new CommentDto.RequestDto());
        model.addAttribute("isLoggedIn", true); // 항상 로그인된 것으로 처리
        
        return "review/detail";
    }

    // 직접 /reviews/write로 접근할 때 기본 confirmedPlanId 값을 설정
    @GetMapping("/write")
    public String writeFormDefault(Model model) {
        Long confirmedPlanId = 1L; // 기본 확정 계획 ID 설정
        return writeForm(confirmedPlanId, model);
    }

    @GetMapping("/write/{confirmedPlanId}")
    public String writeForm(@PathVariable Long confirmedPlanId, Model model) {
        // 확정 계획 정보 조회
        ConfirmedPlanDto.ResponseDto confirmedPlan = confirmedPlanService.findByIdForAnyone(confirmedPlanId);
        
        // 이미 후기가 작성되었는지 확인
        String reviewState = confirmedPlan.isHasReview() ? "ALREADY_REVIEWED" : "NOT_REVIEWED";
        
        switch (reviewState) {
            case "ALREADY_REVIEWED":
                return "redirect:/reviews?error=이미 후기가 작성된 확정 계획입니다.";
            case "NOT_REVIEWED":
                model.addAttribute("confirmedPlanId", confirmedPlanId);
                
                // 빈 필드 초기화
                model.addAttribute("title", "");
                model.addAttribute("content", "");
                
                model.addAttribute("isStar1", true); // 기본값으로 1점 선택
                
                return "review/write";
            default:
                return "redirect:/reviews";
        }
    }

    @PostMapping("/write")
    public String write(@RequestParam("title") String title,
                        @RequestParam("content") String content,
                        @RequestParam("starRating") int starRating,
                        @RequestParam("confirmedPlanId") Long confirmedPlanId,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        // 입력값 검증
        String validationState = getValidationState(title, content);
        
        switch (validationState) {
            case "INVALID":
                model.addAttribute("confirmedPlanId", confirmedPlanId);
                model.addAttribute("title", title);
                model.addAttribute("content", content);
                model.addAttribute("errorMessage", "제목과 내용은 필수 입력 항목입니다.");
                
                // 별점 선택 상태 유지
                addStarRatingAttributes(model, starRating);
                return "review/write";
        }

        // 이미 후기가 작성된 확정 계획인지 확인
        ConfirmedPlanDto.ResponseDto confirmedPlan = confirmedPlanService.findByIdForAnyone(confirmedPlanId);
        
        String reviewState = confirmedPlan.isHasReview() ? "ALREADY_REVIEWED" : "NOT_REVIEWED";
        
        switch (reviewState) {
            case "ALREADY_REVIEWED":
                model.addAttribute("confirmedPlanId", confirmedPlanId);
                model.addAttribute("title", title);
                model.addAttribute("content", content);
                model.addAttribute("errorMessage", "이미 후기가 작성된 확정 계획입니다.");
                
                // 별점 선택 상태 유지
                addStarRatingAttributes(model, starRating);
                return "review/write";
            case "NOT_REVIEWED":
                // ReviewDto.RequestDto 객체 생성
                ReviewDto.RequestDto requestDto = new ReviewDto.RequestDto(title, content, null, starRating, confirmedPlanId);
                
                Long reviewId = reviewService.save(requestDto, DEFAULT_AUTHOR_NAME);
                
                // 성공 메시지 추가
                redirectAttributes.addFlashAttribute("successMessage", "후기가 성공적으로 등록되었습니다.");
                
                // 후기 게시판 목록으로 리다이렉트
                return "redirect:/reviews";
            default:
                return "redirect:/reviews";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ReviewDto.ResponseDto review = reviewService.findById(id, DEFAULT_AUTHOR_NAME);

        model.addAttribute("reviewId", id);
        
        // 기존 데이터를 개별 필드로 모델에 추가
        model.addAttribute("title", review.getTitle());
        model.addAttribute("content", review.getContent());
        
        // 별점 선택 상태 설정
        addStarRatingAttributes(model, review.getStarRating());
        
        return "review/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam("title") String title,
                       @RequestParam("content") String content,
                       @RequestParam("starRating") int starRating,
                       Model model) {
        // 입력값 검증
        String validationState = getValidationState(title, content);
        
        switch (validationState) {
            case "INVALID":
                model.addAttribute("reviewId", id);
                model.addAttribute("title", title);
                model.addAttribute("content", content);
                model.addAttribute("errorMessage", "제목과 내용은 필수 입력 항목입니다.");
                
                // 별점 선택 상태 유지
                addStarRatingAttributes(model, starRating);
                return "review/edit";
            case "VALID":
                // ReviewDto.RequestDto 객체 생성
                ReviewDto.RequestDto requestDto = new ReviewDto.RequestDto(title, content, null, starRating, null);
                
                reviewService.update(id, requestDto, DEFAULT_AUTHOR_NAME);
                return "redirect:/reviews/" + id;
            default:
                return "redirect:/reviews/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        reviewService.delete(id, DEFAULT_AUTHOR_NAME);
        return "redirect:/reviews";
    }

    private void addStarRatingAttributes(Model model, int starRating) {
        model.addAttribute("isStar1", starRating == 1);
        model.addAttribute("isStar2", starRating == 2);
        model.addAttribute("isStar3", starRating == 3);
        model.addAttribute("isStar4", starRating == 4);
        model.addAttribute("isStar5", starRating == 5);
    }

    // 유효성 검증 결과를 문자열로 반환하는 메서드
    private String getValidationState(String title, String content) {
        boolean isInputValid = title != null && !title.trim().isEmpty() 
                            && content != null && !content.trim().isEmpty();
        return isInputValid ? "VALID" : "INVALID";
    }

    // 페이지 번호 DTO
    public static class PageNumberDto {
        private int number;
        private boolean active;
        private int displayNumber;
        
        public PageNumberDto(int number, boolean active, int displayNumber) {
            this.number = number;
            this.active = active;
            this.displayNumber = displayNumber;
        }
        
        public int getNumber() {
            return number;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public int getDisplayNumber() {
            return displayNumber;
        }
    }
} 