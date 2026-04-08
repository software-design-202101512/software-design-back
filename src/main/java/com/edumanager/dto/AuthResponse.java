package com.edumanager.dto;

import com.edumanager.entity.Role;

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
