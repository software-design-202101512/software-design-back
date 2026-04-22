package com.edumanager.user;

import java.time.LocalDateTime;

public record PendingUserResponse(
        Long id,
        String email,
        String name,
        Role role,
        LocalDateTime createdAt
) {
}
