package com.edumanager.auth;

import com.edumanager.user.Role;

public record AuthResponse(
        String token,
        UserDto user
) {
    public record UserDto(
            String id,
            String name,
            String email,
            Role role
    ) {}
}
