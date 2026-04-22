package com.edumanager.record;

import com.edumanager.exception.UnauthorizedException;
import com.edumanager.user.UserRepository;
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
        String authority = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();
        return authority.replace("ROLE_", "");
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

    @PutMapping("/{recordId}")
    public ResponseEntity<StudentRecordResponse> updateRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody StudentRecordRequest request) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 학생부를 수정할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.ok(recordService.updateRecord(recordId, teacherId, request));
    }

    @PatchMapping("/{recordId}/visibility")
    public ResponseEntity<Void> updateVisibility(
            @PathVariable Long recordId,
            @RequestParam boolean visible) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 공개 여부를 변경할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        recordService.updateVisibility(recordId, teacherId, visible);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long recordId) {
        Long teacherId = getCurrentUserId();
        recordService.deleteRecord(recordId, teacherId);
        return ResponseEntity.noContent().build();
    }
}
