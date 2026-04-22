package com.edumanager.consultation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConsultationResponse(
        Long id,
        Long studentId,
        String studentName,
        String teacherName,
        LocalDate consultationDate,
        String mainContent,
        String nextPlan,
        Boolean isSharedWithTeachers,
        LocalDateTime createdAt
) {}
