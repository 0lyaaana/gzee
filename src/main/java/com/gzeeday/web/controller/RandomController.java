package com.gzeeday.web.controller;

import com.gzeeday.service.RecommendationService;
import com.gzeeday.web.dto.plan.ConfirmedPlanDto;
import com.gzeeday.web.dto.recommendation.RecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class RandomController {

    private final RecommendationService recommendationService;

    @GetMapping("/random")
    public String randomPage(Model model) {
        try {
            RecommendationDto.ResponseDto recommendation = recommendationService.findRandom();
            model.addAttribute("recommendation", recommendation);
            model.addAttribute("confirmedPlanRequest", new ConfirmedPlanDto.RequestDto(recommendation.getId()));
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        
        return "random/random";
    }

    @GetMapping("/api/random")
    @ResponseBody
    public RecommendationDto.ResponseDto getRandomRecommendation() {
        return recommendationService.findRandom();
    }
} 