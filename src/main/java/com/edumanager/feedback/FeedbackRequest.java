package com.edumanager.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @NotNull Long studentId,
        @NotBlank String content,
        Boolean isPublicToStudentParent  // 학생/학부모에게 공개 여부
) {}
