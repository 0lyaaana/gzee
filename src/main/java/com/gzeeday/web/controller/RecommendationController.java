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
        String validationState = getValidationState(title, description);
        
        switch (validationState) {
            case "INVALID":
                model.addAttribute("title", title);
                model.addAttribute("description", description);
                model.addAttribute("errorMessage", "제목과 설명은 필수 입력 항목입니다.");
                return "recommendations/new";
            case "VALID":
                RecommendationDto.RequestDto requestDto = new RecommendationDto.RequestDto(title, description);
                recommendationService.save(requestDto, DEFAULT_AUTHOR);
                
                redirectAttributes.addFlashAttribute("successMessage", "새 활동이 성공적으로 추천되었습니다.");
                return "redirect:/recommendations";
            default:
                return "redirect:/recommendations";
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
        RecommendationDto.ResponseDto recommendation = recommendationService.findById(id);

        model.addAttribute("recommendationId", id);
        model.addAttribute("title", recommendation.getTitle());
        model.addAttribute("description", recommendation.getDescription());
        
        return "recommendations/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam("title") String title,
                       @RequestParam("description") String description,
                       Model model) {
        // 입력값 검증
        String validationState = getValidationState(title, description);
        
        switch (validationState) {
            case "INVALID":
                model.addAttribute("recommendationId", id);
                model.addAttribute("title", title);
                model.addAttribute("description", description);
                model.addAttribute("errorMessage", "제목과 설명은 필수 입력 항목입니다.");
                return "recommendations/edit";
            case "VALID":
                // RecommendationDto.RequestDto 객체 생성
                RecommendationDto.RequestDto requestDto = new RecommendationDto.RequestDto(title, description);
                
                recommendationService.update(id, requestDto);
                return "redirect:/recommendations/" + id;
            default:
                return "redirect:/recommendations/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        recommendationService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "활동 추천이 성공적으로 삭제되었습니다.");
        return "redirect:/recommendations";
    }
    
    // 유효성 검증 결과를 문자열로 반환하는 메서드
    private String getValidationState(String title, String description) {
        boolean isInputValid = title != null && !title.trim().isEmpty() 
                            && description != null && !description.trim().isEmpty();
        return isInputValid ? "VALID" : "INVALID";
    }
} 