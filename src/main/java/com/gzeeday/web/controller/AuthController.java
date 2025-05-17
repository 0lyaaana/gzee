package com.gzeeday.web.controller;

import com.gzeeday.service.UserService;
import com.gzeeday.web.dto.token.TokenDto;
import com.gzeeday.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/random";
    }

    @GetMapping("/auth/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new UserDto.LoginRequest());
        return "auth/login";
    }

    @GetMapping("/auth/signup")
    public String signupForm(Model model) {
        model.addAttribute("signUpRequest", new UserDto.SignUpRequest());
        return "auth/signup";
    }

    @PostMapping("/auth/signup")
    public String signup(@Valid @ModelAttribute UserDto.SignUpRequest signUpRequest,
                         BindingResult bindingResult, Model model) {
        log.info("회원가입 요청: {}", signUpRequest);
        
        if (bindingResult.hasErrors()) {
            // 유효성 검사 오류 메시지 수집
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("<br>"));
            log.error("회원가입 유효성 검사 실패: {}", errorMessage);
            model.addAttribute("errorMessage", errorMessage);
            return "auth/signup";
        }

        try {
            userService.signup(signUpRequest);
            return "redirect:/auth/login?signup=success";
        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/signup";
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", "이메일 발송에 실패했습니다. 다시 시도해주세요.");
            return "auth/signup";
        } catch (Exception e) {
            log.error("회원가입 중 예외 발생: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "auth/signup";
        }
    }

    @GetMapping("/auth/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            userService.verifyEmail(token);
            model.addAttribute("message", "이메일 인증이 완료되었습니다. 로그인해주세요.");
            return "auth/verify-email";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/verify-email";
        }
    }

    @GetMapping("/auth/find-password")
    public String findPasswordForm(Model model) {
        model.addAttribute("passwordResetRequest", new UserDto.PasswordResetRequest());
        return "auth/find-password";
    }

    @PostMapping("/auth/find-password")
    public String findPassword(@Valid @ModelAttribute("passwordResetRequest") UserDto.PasswordResetRequest requestDto,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/find-password";
        }

        try {
            userService.requestPasswordReset(requestDto.getEmail());
            model.addAttribute("message", "비밀번호 재설정 링크가 이메일로 발송되었습니다.");
            return "auth/password-reset-sent";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/find-password";
        } catch (MessagingException e) {
            model.addAttribute("errorMessage", "이메일 발송에 실패했습니다. 다시 시도해주세요.");
            return "auth/find-password";
        }
    }

    @GetMapping("/auth/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordRequest", new TokenDto.ResetPasswordRequest());
        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordRequest") TokenDto.ResetPasswordRequest requestDto,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", requestDto.getToken());
            return "auth/reset-password";
        }

        try {
            userService.resetPassword(requestDto.getToken(), requestDto.getNewPassword(), requestDto.getNewPasswordConfirm());
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 로그인해주세요.");
            return "auth/password-reset-sent";
        } catch (IllegalArgumentException e) {
            model.addAttribute("token", requestDto.getToken());
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }
} 