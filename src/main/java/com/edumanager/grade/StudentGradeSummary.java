package com.edumanager.grade;

import java.math.BigDecimal;
import java.util.List;

public record StudentGradeSummary(
        Long studentId,
        String studentName,
        int year,
        int semester,
        List<GradeResponse> grades,
        BigDecimal averageScore,
        BigDecimal totalScore,
        String overallRank  // 평균 점수 기반 종합 9등급
) {}
