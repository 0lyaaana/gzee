package com.gzeeday.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("gzeeday@gmail.com");
        helper.setTo(to);
        helper.setSubject("[GZEE] 이메일 인증을 완료해주세요");
        
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #86af95;'>GZEE 이메일 인증</h2>"
                + "<p>안녕하세요, GZEE에 가입해주셔서 감사합니다.</p>"
                + "<p>아래 링크를 클릭하여 이메일 인증을 완료해주세요.</p>"
                + "<a href='http://localhost:8080/auth/verify-email?token=" + token + "' "
                + "style='display: inline-block; background-color: #86af95; color: white; padding: 10px 20px; "
                + "text-decoration: none; border-radius: 5px; margin: 20px 0;'>이메일 인증하기</a>"
                + "<p>링크는 24시간 동안 유효합니다.</p>"
                + "<p>감사합니다.<br>GZEE 팀</p>"
                + "<div style='margin-top: 30px; padding-top: 10px; border-top: 1px solid #eee;'>"
                + "<p style='color: #777; font-size: 12px;'>귀찮지만 낭만있게, GZEE</p>"
                + "</div></div>";
        
        helper.setText(content, true);
        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("gzeeday@gmail.com");
        helper.setTo(to);
        helper.setSubject("[GZEE] 비밀번호 재설정 안내");
        
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #86af95;'>GZEE 비밀번호 재설정</h2>"
                + "<p>안녕하세요, GZEE 비밀번호 재설정을 요청하셨습니다.</p>"
                + "<p>아래 링크를 클릭하여 비밀번호를 재설정해주세요.</p>"
                + "<a href='http://localhost:8080/auth/reset-password?token=" + token + "' "
                + "style='display: inline-block; background-color: #86af95; color: white; padding: 10px 20px; "
                + "text-decoration: none; border-radius: 5px; margin: 20px 0;'>비밀번호 재설정하기</a>"
                + "<p>링크는 1시간 동안 유효합니다.</p>"
                + "<p>본인이 요청하지 않았다면 이 이메일을 무시해주세요.</p>"
                + "<p>감사합니다.<br>GZEE 팀</p>"
                + "<div style='margin-top: 30px; padding-top: 10px; border-top: 1px solid #eee;'>"
                + "<p style='color: #777; font-size: 12px;'>귀찮지만 낭만있게, GZEE</p>"
                + "</div></div>";
        
        helper.setText(content, true);
        mailSender.send(message);
    }
} 