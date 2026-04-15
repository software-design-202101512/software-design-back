package com.edumanager.record;

import java.time.LocalDateTime;

public record StudentRecordResponse(
        Long id,
        Long studentId,
        String studentName,
        String teacherName,
        Long subjectId,
        String subjectName,
        RecordCategory category,
        String content,
        boolean isVisibleToStudent,
        LocalDateTime createdAt
) {}
