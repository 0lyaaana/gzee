package com.gzeeday.web.controller;

import com.gzeeday.service.RecommendationService;
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
    private static final String DEFAULT_AUTHOR = "사용자";

    @GetMapping
    public String list(Model model) {
        List<RecommendationDto.ListResponseDto> recommendations = recommendationService.findAll();
        
        model.addAttribute("recommendations", recommendations);
        
        return "recommendations/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("title", "");
        model.addAttribute("description", "");
        
        return "recommendations/new";
    }

    @PostMapping("/new")
    public String save(@RequestParam("title") String title,
                       @RequestParam("description") String description,
                       Model model, RedirectAttributes redirectAttributes) {
        // 입력값 검증
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("errorMessage", "제목과 설명은 필수 입력 항목입니다.");
            return "recommendations/new";
        }
        
        try {
            RecommendationDto.RequestDto requestDto = new RecommendationDto.RequestDto(title, description);
            recommendationService.save(requestDto, DEFAULT_AUTHOR);
            
            redirectAttributes.addFlashAttribute("successMessage", "새 활동이 성공적으로 추천되었습니다.");
            return "redirect:/recommendations";
        } catch (Exception e) {
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("errorMessage", "추천 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "recommendations/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        RecommendationDto.ResponseDto recommendation = recommendationService.findById(id);
        
        model.addAttribute("recommendation", recommendation);
        
        return "recommendations/detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            RecommendationDto.ResponseDto recommendation = recommendationService.findById(id);

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
                       Model model) {
        // 입력값 검증
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
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
            model.addAttribute("recommendationId", id);
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("errorMessage", e.getMessage());
            return "recommendations/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            recommendationService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "활동 추천이 성공적으로 삭제되었습니다.");
            return "redirect:/recommendations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/recommendations";
        }
    }
} 