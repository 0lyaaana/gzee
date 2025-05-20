package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.service.RecommendationService;
import com.gzeeday.service.UserService;
import com.gzeeday.web.dto.recommendation.RecommendationDto;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/new")
    public String writeForm(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", userService.findById(user.getId()));
        return "recommendations/write";
    }

    @PostMapping("/new")
    public String write(@RequestParam("title") String title,
                        @RequestParam("description") String description,
                        @LoginUser SessionUser user, Model model,
                        RedirectAttributes redirectAttributes) {
        // 입력값 검증
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("errorMessage", "제목과 설명은 필수 입력 항목입니다.");
            return "recommendations/write";
        }
        
        try {
            // RecommendationDto.RequestDto 객체 생성
            RecommendationDto.RequestDto requestDto = new RecommendationDto.RequestDto(title, description);
            
            Long recommendationId = recommendationService.save(requestDto, user != null ? user.getId() : null);
            
            // 성공 메시지 추가
            redirectAttributes.addFlashAttribute("successMessage", "활동 추천이 성공적으로 등록되었습니다.");
            
            // 활동 추천 목록으로 리다이렉트
            return "redirect:/recommendations";
        } catch (Exception e) {
            if (user != null) {
                model.addAttribute("user", userService.findById(user.getId()));
            }
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("errorMessage", "활동 추천 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "recommendations/write";
        }
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