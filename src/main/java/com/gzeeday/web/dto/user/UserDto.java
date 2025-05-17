package com.gzeeday.web.dto.user;

import com.gzeeday.domain.user.Role;
import com.gzeeday.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserDto {

    @Getter
    @NoArgsConstructor
    public static class SignUpRequest {
        @NotBlank(message = "이름을 입력해주세요")
        private String name;

        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이로 입력해주세요")
        private String nickname;

        @NotBlank(message = "아이디를 입력해주세요")
        @Size(min = 4, max = 20, message = "아이디는 4~20자 사이로 입력해주세요")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용 가능합니다")
        private String username;

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이로 입력해주세요")
        private String password;

        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        private String passwordConfirm;

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        private String email;

        @NotBlank(message = "전화번호를 입력해주세요")
        @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "전화번호 형식이 올바르지 않습니다")
        private String phone;

        @Builder
        public SignUpRequest(String name, String nickname, String username, String password, String passwordConfirm, String email, String phone) {
            this.name = name;
            this.nickname = nickname;
            this.username = username;
            this.password = password;
            this.passwordConfirm = passwordConfirm;
            this.email = email;
            this.phone = phone;
        }

        public User toEntity(String encodedPassword) {
            return User.builder()
                    .name(name)
                    .nickname(nickname)
                    .username(username)
                    .password(encodedPassword)
                    .email(email)
                    .phone(phone)
                    .role(Role.USER)
                    .emailVerified(false)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "아이디를 입력해주세요")
        private String username;

        @NotBlank(message = "비밀번호를 입력해주세요")
        private String password;

        @Builder
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UserResponse {
        private Long id;
        private String name;
        private String nickname;
        private String username;
        private String email;
        private String phone;
        private String role;
        private boolean emailVerified;

        @Builder
        public UserResponse(User entity) {
            this.id = entity.getId();
            this.name = entity.getName();
            this.nickname = entity.getNickname();
            this.username = entity.getUsername();
            this.email = entity.getEmail();
            this.phone = entity.getPhone();
            this.role = entity.getRole().getTitle();
            this.emailVerified = entity.isEmailVerified();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "이름을 입력해주세요")
        private String name;

        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이로 입력해주세요")
        private String nickname;

        @NotBlank(message = "전화번호를 입력해주세요")
        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다")
        private String phone;

        @Builder
        public UpdateRequest(String name, String nickname, String phone) {
            this.name = name;
            this.nickname = nickname;
            this.phone = phone;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PasswordResetRequest {
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        private String email;

        @Builder
        public PasswordResetRequest(String email) {
            this.email = email;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PasswordChangeRequest {
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이로 입력해주세요")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
                message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
        private String newPasswordConfirm;

        @Builder
        public PasswordChangeRequest(String currentPassword, String newPassword, String newPasswordConfirm) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
            this.newPasswordConfirm = newPasswordConfirm;
        }
    }
}