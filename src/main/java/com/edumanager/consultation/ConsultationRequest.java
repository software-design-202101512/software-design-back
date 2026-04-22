package com.edumanager.consultation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConsultationRequest(
        @NotNull Long studentId,
        @NotNull LocalDate consultationDate,
        @NotBlank String mainContent,
        String nextPlan,
        Boolean isSharedWithTeachers  // 다른 교사에게 공유 여부
) {}
