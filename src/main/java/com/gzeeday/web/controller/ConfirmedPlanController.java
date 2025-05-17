package com.gzeeday.web.controller;

import com.gzeeday.config.auth.LoginUser;
import com.gzeeday.config.auth.dto.SessionUser;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.service.ConfirmedPlanService;
import com.gzeeday.web.dto.plan.ConfirmedPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ConfirmedPlanController {

    private final ConfirmedPlanService confirmedPlanService;
    private final UserRepository userRepository;

    @PostMapping("/api/confirmed-plan")
    @ResponseBody
    public ResponseEntity<?> confirm(@RequestBody ConfirmedPlanDto.RequestDto requestDto, @LoginUser SessionUser user) {
        // 로그인 체크 제거
        Long userId = 1L; // 기본 사용자 ID 설정 (비로그인 사용자용)
        
        try {
            Long id = confirmedPlanService.confirm(requestDto, user != null ? user.getId() : userId);
            return ResponseEntity.ok(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 