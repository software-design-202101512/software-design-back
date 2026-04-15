package com.edumanager.grade;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GradeRequest(
        @NotNull Long studentId,
        @NotNull Long subjectId,
        @NotNull Integer year,
        @NotNull Integer semester,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal score,
        String rank
) {}
