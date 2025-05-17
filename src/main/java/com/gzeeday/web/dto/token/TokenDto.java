package com.gzeeday.web.dto.token;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class TokenDto {

    @Getter
    @NoArgsConstructor
    public static class VerifyEmailRequest {
        @NotBlank(message = "토큰을 입력해주세요")
        private String token;

        @Builder
        public VerifyEmailRequest(String token) {
            this.token = token;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "토큰을 입력해주세요")
        private String token;

        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이로 입력해주세요")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
                message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
        private String newPasswordConfirm;

        @Builder
        public ResetPasswordRequest(String token, String newPassword, String newPasswordConfirm) {
            this.token = token;
            this.newPassword = newPassword;
            this.newPasswordConfirm = newPasswordConfirm;
        }
    }
} 