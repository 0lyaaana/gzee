package com.gzeeday.web.controller;

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
    
    // 기본 사용자명 설정
    private static final String DEFAULT_PLANNER_NAME = "사용자";

    @PostMapping("/api/confirmed-plan")
    @ResponseBody
    public ResponseEntity<?> confirm(@RequestBody ConfirmedPlanDto.RequestDto requestDto) {
        try {
            Long id = confirmedPlanService.confirm(requestDto, DEFAULT_PLANNER_NAME);
            return ResponseEntity.ok(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 