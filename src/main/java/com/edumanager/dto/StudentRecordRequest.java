package com.edumanager.dto;

import com.edumanager.entity.RecordCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentRecordRequest(
        @NotNull Long studentId,
        Long subjectId,
        @NotNull RecordCategory category,
        @NotBlank String content,
        Boolean isVisibleToStudent
) {}
