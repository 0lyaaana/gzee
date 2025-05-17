package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.service.ConfirmedPlanService;
import com.gzeeday.service.ReviewService;
import com.gzeeday.service.UserService;
import com.gzeeday.web.dto.plan.ConfirmedPlanDto;
import com.gzeeday.web.dto.review.ReviewDto;
import com.gzeeday.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final UserService userService;
    private final ConfirmedPlanService confirmedPlanService;
    private final ReviewService reviewService;

    @GetMapping
    public String myPage(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        UserDto.UserResponse userInfo = userService.findById(user.getId());
        model.addAttribute("user", userInfo);

        return "mypage/index";
    }

    @GetMapping("/profile")
    public String profileForm(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        UserDto.UserResponse userInfo = userService.findById(user.getId());
        model.addAttribute("user", userInfo);
        model.addAttribute("updateRequest", new UserDto.UpdateRequest(userInfo.getName(), userInfo.getNickname(), userInfo.getPhone()));

        return "mypage/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("updateRequest") UserDto.UpdateRequest requestDto,
                               BindingResult bindingResult, @LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.findById(user.getId()));
            return "mypage/profile";
        }

        try {
            userService.updateProfile(user.getId(), requestDto);
            return "redirect:/mypage?update=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", userService.findById(user.getId()));
            model.addAttribute("errorMessage", e.getMessage());
            return "mypage/profile";
        }
    }

    @GetMapping("/password")
    public String passwordForm(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("user", userService.findById(user.getId()));
        model.addAttribute("passwordChangeRequest", new UserDto.PasswordChangeRequest());

        return "mypage/password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeRequest") UserDto.PasswordChangeRequest requestDto,
                                BindingResult bindingResult, @LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.findById(user.getId()));
            return "mypage/password";
        }

        try {
            userService.changePassword(user.getId(), requestDto.getCurrentPassword(), 
                                      requestDto.getNewPassword(), requestDto.getNewPasswordConfirm());
            return "redirect:/mypage?password=success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", userService.findById(user.getId()));
            model.addAttribute("errorMessage", e.getMessage());
            return "mypage/password";
        }
    }

    @GetMapping("/plans")
    public String myPlans(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<ConfirmedPlanDto.ResponseDto> plans = confirmedPlanService.findAllByUser(user.getId());
        model.addAttribute("user", userService.findById(user.getId()));
        model.addAttribute("plans", plans);

        return "mypage/plans";
    }

    @GetMapping("/reviews")
    public String myReviews(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<ReviewDto.ListResponseDto> reviews = reviewService.findAllByUser(user.getId());
        model.addAttribute("user", userService.findById(user.getId()));
        model.addAttribute("reviews", reviews);

        return "mypage/reviews";
    }
} 