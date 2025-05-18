package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.service.CommentService;
import com.gzeeday.service.ConfirmedPlanService;
import com.gzeeday.service.ReviewService;
import com.gzeeday.service.UserService;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final CommentService commentService;
    private final ConfirmedPlanService confirmedPlanService;
    private final UserService userService;

    // 파일 업로드 디렉토리
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       @LoginUser SessionUser user, Model model) {
        Page<ReviewDto.ListResponseDto> reviews = reviewService.findAllPaging(pageable);
        
        if (user != null) {
            model.addAttribute("user", userService.findById(user.getId()));
        }
        
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
        
        return "review/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        Long userId = user != null ? user.getId() : null;
        ReviewDto.ResponseDto review = reviewService.findById(id, userId);
        List<CommentDto.ResponseDto> comments = commentService.findAllByReview(id, userId);
        
        if (user != null) {
            model.addAttribute("user", userService.findById(user.getId()));
        }
        
        model.addAttribute("review", review);
        model.addAttribute("comments", comments);
        model.addAttribute("commentRequest", new CommentDto.RequestDto());
        model.addAttribute("isLoggedIn", user != null);
        
        return "review/detail";
    }

    // 직접 /reviews/write로 접근할 때 기본 confirmedPlanId 값을 설정
    @GetMapping("/write")
    public String writeFormDefault(@LoginUser SessionUser user, Model model) {
        Long confirmedPlanId = 1L; // 기본 확정 계획 ID 설정
        return writeForm(confirmedPlanId, user, model);
    }

    @GetMapping("/write/{confirmedPlanId}")
    public String writeForm(@PathVariable Long confirmedPlanId, @LoginUser SessionUser user, Model model) {
        Long userId = 1L; // 기본 사용자 ID 설정
        
        try {
            // 확정 계획 정보 조회
            ConfirmedPlanDto.ResponseDto confirmedPlan = confirmedPlanService.findById(confirmedPlanId, user != null ? user.getId() : userId);
            
            // 이미 후기가 작성되었는지 확인
            if (confirmedPlan.isHasReview()) {
                return "redirect:/reviews?error=이미 후기가 작성된 확정 계획입니다.";
            }
            
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            
            model.addAttribute("confirmedPlanId", confirmedPlanId);
            
            // 빈 필드 초기화
            model.addAttribute("title", "");
            model.addAttribute("content", "");
            
            model.addAttribute("isStar1", true); // 기본값으로 1점 선택
            
            return "review/write";
        } catch (IllegalArgumentException e) {
            return "redirect:/reviews?error=" + e.getMessage();
        }
    }

    @PostMapping("/write")
    public String write(@RequestParam("title") String title,
                        @RequestParam("content") String content,
                        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                        @RequestParam("starRating") int starRating,
                        @RequestParam("confirmedPlanId") Long confirmedPlanId,
                        @LoginUser SessionUser user, Model model,
                        RedirectAttributes redirectAttributes) {
        Long userId = 1L; // 기본 사용자 ID 설정
        
        // 입력값 검증
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("confirmedPlanId", confirmedPlanId);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("errorMessage", "제목과 내용은 필수 입력 항목입니다.");
            
            // 별점 선택 상태 유지
            addStarRatingAttributes(model, starRating);
            return "review/write";
        }
        
        try {
            // 이미 후기가 작성된 확정 계획인지 확인
            ConfirmedPlanDto.ResponseDto confirmedPlan = confirmedPlanService.findByIdForAnyone(confirmedPlanId);
            if (confirmedPlan.isHasReview()) {
                throw new IllegalArgumentException("이미 후기가 작성된 확정 계획입니다.");
            }
            
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = saveImage(imageFile);
            }
            
            // ReviewDto.RequestDto 객체 생성
            ReviewDto.RequestDto requestDto = new ReviewDto.RequestDto(title, content, imageUrl, starRating, confirmedPlanId);
            
            Long reviewId = reviewService.save(requestDto, user != null ? user.getId() : userId);
            
            // 성공 메시지 추가
            redirectAttributes.addFlashAttribute("successMessage", "후기가 성공적으로 등록되었습니다.");
            
            // 후기 게시판 목록으로 리다이렉트
            return "redirect:/reviews";
        } catch (IllegalArgumentException e) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("confirmedPlanId", confirmedPlanId);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("errorMessage", e.getMessage());
            
            // 별점 선택 상태 유지
            addStarRatingAttributes(model, starRating);
            return "review/write";
        } catch (Exception e) {
            // 기타 예외 처리
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("confirmedPlanId", confirmedPlanId);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("errorMessage", "후기 작성 중 오류가 발생했습니다: " + e.getMessage());
            
            // 별점 선택 상태 유지
            addStarRatingAttributes(model, starRating);
            return "review/write";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        try {
            ReviewDto.ResponseDto review = reviewService.findById(id, user != null ? user.getId() : 1L);
            
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("reviewId", id);
            
            // 기존 데이터를 개별 필드로 모델에 추가
            model.addAttribute("title", review.getTitle());
            model.addAttribute("content", review.getContent());
            model.addAttribute("imageUrl", review.getImageUrl());
            
            // 별점 선택 상태 설정
            addStarRatingAttributes(model, review.getStarRating());
            
            return "review/edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/reviews?error=" + e.getMessage();
        }
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam("title") String title,
                       @RequestParam("content") String content,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       @RequestParam(value = "existingImageUrl", required = false) String existingImageUrl,
                       @RequestParam("starRating") int starRating,
                       @LoginUser SessionUser user, Model model) {
        // 입력값 검증
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("reviewId", id);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("imageUrl", existingImageUrl);
            model.addAttribute("errorMessage", "제목과 내용은 필수 입력 항목입니다.");
            
            // 별점 선택 상태 유지
            addStarRatingAttributes(model, starRating);
            return "review/edit";
        }
        
        try {
            String imageUrl = existingImageUrl;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = saveImage(imageFile);
            }
            
            // ReviewDto.RequestDto 객체 생성
            ReviewDto.RequestDto requestDto = new ReviewDto.RequestDto(title, content, imageUrl, starRating, null);
            
            reviewService.update(id, requestDto, user != null ? user.getId() : 1L);
            return "redirect:/reviews/" + id;
        } catch (IllegalArgumentException e) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("reviewId", id);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("imageUrl", existingImageUrl);
            model.addAttribute("errorMessage", e.getMessage());
            
            // 별점 선택 상태 유지
            addStarRatingAttributes(model, starRating);
            return "review/edit";
        } catch (Exception e) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("reviewId", id);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("imageUrl", existingImageUrl);
            model.addAttribute("errorMessage", "후기 수정 중 오류가 발생했습니다: " + e.getMessage());
            
            // 별점 선택 상태 유지
            addStarRatingAttributes(model, starRating);
            return "review/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @LoginUser SessionUser user) {
        try {
            reviewService.delete(id, user != null ? user.getId() : 1L);
            return "redirect:/reviews?delete=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/reviews/" + id + "?error=" + e.getMessage();
        }
    }

    // 별점 선택 상태를 모델에 추가하는 헬퍼 메서드
    private void addStarRatingAttributes(Model model, int starRating) {
        model.addAttribute("isStar1", starRating == 1);
        model.addAttribute("isStar2", starRating == 2);
        model.addAttribute("isStar3", starRating == 3);
        model.addAttribute("isStar4", starRating == 4);
        model.addAttribute("isStar5", starRating == 5);
    }
    
    // 이미지 파일 저장 메서드
    private String saveImage(MultipartFile file) throws IOException {
        // 업로드 디렉토리가 없으면 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 파일명 충돌 방지를 위한 고유 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        
        // 파일 저장
        Files.write(path, file.getBytes());
        
        // 웹에서 접근 가능한 URL 경로 반환
        return "/uploads/" + fileName;
    }
} 