package com.edumanager.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentGradeSummary(
        Long studentId,
        String studentName,
        int year,
        int semester,
        List<GradeResponse> grades,
        BigDecimal averageScore,
        BigDecimal totalScore
) {}
