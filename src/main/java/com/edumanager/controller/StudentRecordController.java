package com.edumanager.controller;

import com.edumanager.dto.StudentRecordRequest;
import com.edumanager.dto.StudentRecordResponse;
import com.edumanager.exception.UnauthorizedException;
import com.edumanager.repository.UserRepository;
import com.edumanager.service.StudentRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class StudentRecordController {

    private final StudentRecordService recordService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();
    }

    @PostMapping
    public ResponseEntity<StudentRecordResponse> createRecord(@Valid @RequestBody StudentRecordRequest request) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 학생부를 작성할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.createRecord(teacherId, request));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentRecordResponse>> getRecords(@PathVariable Long studentId) {
        Long requesterId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ResponseEntity.ok(recordService.getRecordsByStudent(studentId, requesterId, role));
    }

    @PatchMapping("/{recordId}/visibility")
    public ResponseEntity<StudentRecordResponse> updateVisibility(
            @PathVariable Long recordId,
            @RequestParam boolean visible) {
        Long teacherId = getCurrentUserId();
        return ResponseEntity.ok(recordService.updateVisibility(recordId, teacherId, visible));
    }
}
