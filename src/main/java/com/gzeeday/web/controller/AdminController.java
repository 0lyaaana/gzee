package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.service.RecommendationService;
import com.gzeeday.service.UserService;
import com.gzeeday.web.dto.recommendation.RecommendationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    @GetMapping
    public String adminHome(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (!user.getRole().equals("관리자")) {
            return "redirect:/?error=권한이 없습니다.";
        }
        
        model.addAttribute("user", userService.findById(user.getId()));
        return "admin/index";
    }

    @GetMapping("/recommendations")
    public String recommendationList(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (!user.getRole().equals("관리자")) {
            return "redirect:/?error=권한이 없습니다.";
        }
        
        List<RecommendationDto.ListResponseDto> recommendations = recommendationService.findAll();
        
        model.addAttribute("user", userService.findById(user.getId()));
        model.addAttribute("recommendations", recommendations);
        return "admin/recommendation-list";
    }

    @GetMapping("/recommendations/add")
    public String addRecommendationForm(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (!user.getRole().equals("관리자")) {
            return "redirect:/?error=권한이 없습니다.";
        }
        
        model.addAttribute("user", userService.findById(user.getId()));
        model.addAttribute("recommendationRequest", new RecommendationDto.RequestDto());
        return "admin/recommendation-add";
    }

    @PostMapping("/recommendations/add")
    public String addRecommendation(@Valid @ModelAttribute("recommendationRequest") RecommendationDto.RequestDto requestDto,
                                    BindingResult bindingResult, @LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (!user.getRole().equals("관리자")) {
            return "redirect:/?error=권한이 없습니다.";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.findById(user.getId()));
            return "admin/recommendation-add";
        }
        
        try {
            recommendationService.save(requestDto, user.getId());
            return "redirect:/admin/recommendations?add=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", userService.findById(user.getId()));
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/recommendation-add";
        }
    }

    @GetMapping("/recommendations/edit/{id}")
    public String editRecommendationForm(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (!user.getRole().equals("관리자")) {
            return "redirect:/?error=권한이 없습니다.";
        }
        
        try {
            RecommendationDto.ResponseDto recommendation = recommendationService.findById(id);
            
            model.addAttribute("user", userService.findById(user.getId()));
            model.addAttribute("recommendationId", id);
            model.addAttribute("recommendationRequest", new RecommendationDto.RequestDto(
                    recommendation.getTitle(), recommendation.getDescription()));
            
            return "admin/recommendation-edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/recommendations?error=" + e.getMessage();
        }
    }

    @PostMapping("/recommendations/edit/{id}")
    public String editRecommendation(@PathVariable Long id, 
                                     @Valid @ModelAttribute("recommendationRequest") RecommendationDto.RequestDto requestDto,
                                     BindingResult bindingResult, @LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (!user.getRole().equals("관리자")) {
            return "redirect:/?error=권한이 없습니다.";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.findById(user.getId()));
            model.addAttribute("recommendationId", id);
            return "admin/recommendation-edit";
        }
        
        try {
            recommendationService.update(id, requestDto);
            return "redirect:/admin/recommendations?edit=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", userService.findById(user.getId()));
            model.addAttribute("recommendationId", id);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/recommendation-edit";
        }
    }

    @PostMapping("/recommendations/delete/{id}")
    public String deleteRecommendation(@PathVariable Long id, @LoginUser SessionUser user) {
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (!user.getRole().equals("관리자")) {
            return "redirect:/?error=권한이 없습니다.";
        }
        
        try {
            recommendationService.delete(id);
            return "redirect:/admin/recommendations?delete=success";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/recommendations?error=" + e.getMessage();
        }
    }
} 