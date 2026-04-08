package com.edumanager.dto;

import com.edumanager.entity.RecordCategory;

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
