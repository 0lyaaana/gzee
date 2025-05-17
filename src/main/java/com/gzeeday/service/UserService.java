package com.gzeeday.service;

import com.gzeeday.domain.token.EmailVerificationToken;
import com.gzeeday.domain.token.EmailVerificationTokenRepository;
import com.gzeeday.domain.token.PasswordResetToken;
import com.gzeeday.domain.token.PasswordResetTokenRepository;
import com.gzeeday.domain.user.User;
import com.gzeeday.domain.user.UserRepository;
import com.gzeeday.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository passwordTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public Long signup(UserDto.SignUpRequest requestDto) throws MessagingException {
        // 아이디 중복 확인
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 전화번호 중복 확인
        if (userRepository.existsByPhone(requestDto.getPhone())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        // 비밀번호 확인
        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        
        // 사용자 저장
        User user = requestDto.toEntity(encodedPassword);
        userRepository.save(user);
        
        // 이메일 인증 토큰 생성
        String token = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        emailTokenRepository.save(emailToken);
        
        // 인증 이메일 발송
        emailService.sendVerificationEmail(user.getEmail(), token);
        
        return user.getId();
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken emailToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
        
        if (!emailToken.isValid()) {
            throw new IllegalArgumentException("만료되었거나 이미 사용된 토큰입니다.");
        }
        
        User user = emailToken.getUser();
        user.verifyEmail();
        emailToken.useToken();
    }

    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));
        
        // 기존 토큰이 있다면 만료 처리
        passwordTokenRepository.findByUserAndIsUsedFalse(user).ifPresent(PasswordResetToken::useToken);
        
        // 새로운 토큰 생성
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        passwordTokenRepository.save(passwordToken);
        
        // 비밀번호 재설정 이메일 발송
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
        }
        
        PasswordResetToken passwordToken = passwordTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
        
        if (!passwordToken.isValid()) {
            throw new IllegalArgumentException("만료되었거나 이미 사용된 토큰입니다.");
        }
        
        User user = passwordToken.getUser();
        user.updatePassword(passwordEncoder.encode(newPassword));
        passwordToken.useToken();
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String newPasswordConfirm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
        }
        
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void updateProfile(Long userId, UserDto.UpdateRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        user.updateProfile(requestDto.getName(), requestDto.getNickname(), requestDto.getPhone());
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return new UserDto.UserResponse(user);
    }
} 