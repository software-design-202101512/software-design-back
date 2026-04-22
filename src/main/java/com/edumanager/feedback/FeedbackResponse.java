package com.edumanager.feedback;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Long studentId,
        String studentName,
        String teacherName,
        String content,
        Boolean isPublicToStudentParent,
        LocalDateTime createdAt
) {}
