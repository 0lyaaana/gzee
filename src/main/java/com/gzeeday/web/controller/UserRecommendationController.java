package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.service.UserRecommendationService;
import com.gzeeday.service.UserService;
import com.gzeeday.web.dto.recommendation.UserRecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class UserRecommendationController {

    private final UserRecommendationService userRecommendationService;
    private final UserService userService;

    // 새 활동 추천 폼
    @GetMapping("/new")
    public String newForm(@LoginUser SessionUser user, Model model) {
        if (user != null) {
            model.addAttribute("user", userService.findById(user.getId()));
        }
        // 빈 문자열로 초기화
        model.addAttribute("title", "");
        model.addAttribute("content", "");
        model.addAttribute("reason", "");

        return "recommendations/new";
    }

    // 새 활동 추천 저장
    @PostMapping("/new")
    public String save(@RequestParam("title") String title,
                       @RequestParam("content") String content,
                       @RequestParam("reason") String reason,
                       @LoginUser SessionUser user,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        // 입력값 검증
        if (title == null || title.trim().isEmpty() || 
            content == null || content.trim().isEmpty() || 
            reason == null || reason.trim().isEmpty()) {
            
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            
            // 입력값 유지를 위해 다시 모델에 추가
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("reason", reason);
            model.addAttribute("errorMessage", "모든 필수 항목을 입력해주세요.");
            
            return "recommendations/new";
        }

        try {
            Long userId = user != null ? user.getId() : 1L; // 비로그인 사용자는 기본 사용자 ID 사용
            
            UserRecommendationDto.RequestDto requestDto = UserRecommendationDto.RequestDto.builder()
                .title(title)
                .content(content)
                .reason(reason)
                .build();
                
            userRecommendationService.save(requestDto, userId);
            redirectAttributes.addFlashAttribute("successMessage", "새 활동 추천이 등록되었습니다. 검토 후 추가됩니다.");
            
            // 랜덤 페이지로 리다이렉트
            return "redirect:/random";
        } catch (Exception e) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            
            // 입력값 유지를 위해 다시 모델에 추가
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("reason", reason);
            model.addAttribute("errorMessage", "등록 중 오류가 발생했습니다: " + e.getMessage());
            
            return "recommendations/new";
        }
    }

    // 사용자 추천 목록
    @GetMapping("/user-list")
    public String userList(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(value = "status", required = false) String status,
                           @LoginUser SessionUser user,
                           Model model) {
        Page<UserRecommendationDto.ListResponseDto> userRecommendations;
        
        if (status != null) {
            switch (status) {
                case "pending":
                    userRecommendations = userRecommendationService.findAllByStatus(pageable, "PENDING");
                    break;
                case "approved":
                    userRecommendations = userRecommendationService.findAllByStatus(pageable, "APPROVED");
                    break;
                case "rejected":
                    userRecommendations = userRecommendationService.findAllByStatus(pageable, "REJECTED");
                    break;
                default:
                    userRecommendations = userRecommendationService.findAllPaging(pageable);
            }
        } else {
            userRecommendations = userRecommendationService.findAllPaging(pageable);
        }

        if (user != null) {
            model.addAttribute("user", userService.findById(user.getId()));
        }

        model.addAttribute("userRecommendations", userRecommendations);
        model.addAttribute("currentStatus", status != null ? status : "all");

        return "recommendations/user-list";
    }

    // 사용자 추천 상세
    @GetMapping("/user/{id}")
    public String userDetail(@PathVariable Long id,
                             @LoginUser SessionUser user,
                             Model model) {
        UserRecommendationDto.ResponseDto userRecommendation = userRecommendationService.findById(id, user != null ? user.getId() : null);

        if (user != null) {
            model.addAttribute("user", userService.findById(user.getId()));
        }

        model.addAttribute("userRecommendation", userRecommendation);

        return "recommendations/user-detail";
    }

    // 승인
    @PostMapping("/user/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userRecommendationService.approve(id);
            redirectAttributes.addFlashAttribute("successMessage", "추천 활동이 승인되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "승인 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/recommendations/user-list";
    }

    // 거부
    @PostMapping("/user/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userRecommendationService.reject(id);
            redirectAttributes.addFlashAttribute("successMessage", "추천 활동이 기각되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "기각 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/recommendations/user-list";
    }

    // 삭제
    @PostMapping("/user/{id}/delete")
    public String delete(@PathVariable Long id,
                         @LoginUser SessionUser user,
                         RedirectAttributes redirectAttributes) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            userRecommendationService.delete(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "추천 활동이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/recommendations";
    }
} 