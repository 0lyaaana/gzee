package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.service.RecommendationService;
import com.gzeeday.service.UserRecommendationService;
import com.gzeeday.service.UserService;
import com.gzeeday.web.dto.recommendation.RecommendationDto;
import com.gzeeday.web.dto.recommendation.UserRecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRecommendationService userRecommendationService;
    private final UserService userService;

    @GetMapping
    public String list(@LoginUser SessionUser user, Model model) {
        List<RecommendationDto.ListResponseDto> recommendations = recommendationService.findAll();
        
        if (user != null) {
            model.addAttribute("user", userService.findById(user.getId()));
        }
        
        model.addAttribute("recommendations", recommendations);
        
        return "recommendations/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        RecommendationDto.ResponseDto recommendation = recommendationService.findById(id);
        
        if (user != null) {
            model.addAttribute("user", userService.findById(user.getId()));
        }
        
        model.addAttribute("recommendation", recommendation);
        
        return "recommendations/detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        try {
            RecommendationDto.ResponseDto recommendation = recommendationService.findById(id);
            
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("recommendationId", id);
            model.addAttribute("title", recommendation.getTitle());
            model.addAttribute("description", recommendation.getDescription());
            
            return "recommendations/edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/recommendations?error=" + e.getMessage();
        }
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam("title") String title,
                       @RequestParam("description") String description,
                       @LoginUser SessionUser user, Model model) {
        // 입력값 검증
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("recommendationId", id);
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("errorMessage", "제목과 설명은 필수 입력 항목입니다.");
            return "recommendations/edit";
        }
        
        try {
            // RecommendationDto.RequestDto 객체 생성
            RecommendationDto.RequestDto requestDto = new RecommendationDto.RequestDto(title, description);
            
            recommendationService.update(id, requestDto);
            return "redirect:/recommendations/" + id;
        } catch (IllegalArgumentException e) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("recommendationId", id);
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("errorMessage", e.getMessage());
            return "recommendations/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @LoginUser SessionUser user, RedirectAttributes redirectAttributes) {
        try {
            recommendationService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "활동 추천이 성공적으로 삭제되었습니다.");
            return "redirect:/recommendations";
        } catch (IllegalArgumentException e) {
            return "redirect:/recommendations/" + id + "?error=" + e.getMessage();
        }
    }
} 