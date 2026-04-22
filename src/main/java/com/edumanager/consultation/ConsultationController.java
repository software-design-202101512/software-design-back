package com.edumanager.consultation;

import com.edumanager.exception.UnauthorizedException;
import com.edumanager.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상담 기록 API
 * - POST   /api/consultations       → 교사만 상담 기록 작성
 * - GET    /api/consultations/student/{id} → 학생별 상담 기록 조회 (역할별 접근)
 * - GET    /api/consultations/my     → 교사 본인 작성 상담 목록
 */
@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
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

    /** 교사만 상담 기록 작성 가능 */
    @PostMapping
    public ResponseEntity<ConsultationResponse> createConsultation(
            @Valid @RequestBody ConsultationRequest request) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 상담 기록을 작성할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.createConsultation(teacherId, request));
    }

    /** 학생별 상담 기록 조회 (역할에 따라 공개 여부 필터링) */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ConsultationResponse>> getByStudent(@PathVariable Long studentId) {
        Long requesterId = getCurrentUserId();
        String requesterRole = getCurrentUserRole();
        return ResponseEntity.ok(consultationService.getConsultationsByStudent(studentId, requesterId, requesterRole));
    }

    /** 교사 본인이 작성한 상담 목록 */
    @GetMapping("/my")
    public ResponseEntity<List<ConsultationResponse>> getMyConsultations() {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 조회할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.ok(consultationService.getConsultationsByTeacher(teacherId));
    }

    /** 담당 학생의 다른 교사가 공유한 상담 기록 조회 */
    @GetMapping("/shared")
    public ResponseEntity<List<ConsultationResponse>> getSharedConsultations() {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 조회할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.ok(consultationService.getSharedConsultations(teacherId));
    }
}
