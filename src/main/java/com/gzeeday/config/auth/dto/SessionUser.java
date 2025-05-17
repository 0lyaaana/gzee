package com.gzeeday.config.auth.dto;

import com.gzeeday.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String role;
    private boolean emailVerified;

    public SessionUser(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRoleKey();
        this.emailVerified = user.isEmailVerified();
    }
} 