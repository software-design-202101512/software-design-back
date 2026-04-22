package com.edumanager.auth;

import com.edumanager.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotNull(message = "역할은 필수입니다.")
        Role role,

        // TEACHER
        String subject,
        String grade,

        // STUDENT
        String classNum,
        String studentNum,
        String gender,

        // PARENT / Profile Info
        String invitationCode,
        String phoneNumber,
        String relationship,
        String childName
) {
}
