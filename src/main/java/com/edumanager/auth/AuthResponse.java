package com.edumanager.auth;

import com.edumanager.user.Role;

public record AuthResponse(
        String token,
        UserDto user,
        String message
) {
    // 기존 호환을 위한 2-arg 생성자
    public AuthResponse(String token, UserDto user) {
        this(token, user, null);
    }

    public record UserDto(
            String id,
            String name,
            String email,
            Role role
    ) {}
}
