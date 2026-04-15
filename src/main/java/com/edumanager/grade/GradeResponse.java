package com.edumanager.grade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GradeResponse(
        Long id,
        Long studentId,
        String studentName,
        Long subjectId,
        String subjectName,
        int year,
        int semester,
        BigDecimal score,
        String rank,
        String teacherName,
        LocalDateTime createdAt
) {}
