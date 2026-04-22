package com.edumanager.grade;

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
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
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
    public ResponseEntity<GradeResponse> createGrade(@Valid @RequestBody GradeRequest request) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 성적을 입력할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.createGrade(teacherId, request));
    }

    @PutMapping("/{gradeId}")
    public ResponseEntity<GradeResponse> updateGrade(
            @PathVariable Long gradeId,
            @Valid @RequestBody GradeRequest request) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 성적을 수정할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.ok(gradeService.updateGrade(gradeId, teacherId, request));
    }

    @DeleteMapping("/{gradeId}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 성적을 삭제할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        gradeService.deleteGrade(gradeId, teacherId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeResponse>> getGradesByStudent(@PathVariable Long studentId) {
        Long requesterId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ResponseEntity.ok(gradeService.getGradesByStudent(studentId, requesterId, role));
    }

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<StudentGradeSummary> getSummary(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int semester) {
        return ResponseEntity.ok(gradeService.getStudentGradeSummary(studentId, year, semester));
    }
}
