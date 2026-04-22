package com.edumanager.feedback;

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
 * 피드백 API
 * - POST   /api/feedbacks             → 교사만 피드백 작성 (Write)
 * - GET    /api/feedbacks/student/{id} → 학생별 피드백 조회 (역할별 Read)
 * - GET    /api/feedbacks/my           → 교사 본인 작성 피드백 목록
 */
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
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

    /** 교사만 피드백 작성 가능 */
    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(
            @Valid @RequestBody FeedbackRequest request) {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 피드백을 작성할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.createFeedback(teacherId, request));
    }

    /**
     * 학생별 피드백 조회
     * - 학생/학부모는 공개 피드백만 조회 가능 (Read Only)
     * - 교사는 전체 피드백 조회 가능
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<FeedbackResponse>> getByStudent(@PathVariable Long studentId) {
        Long requesterId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ResponseEntity.ok(feedbackService.getFeedbacksByStudent(studentId, requesterId, role));
    }

    /** 교사 본인이 작성한 피드백 목록 */
    @GetMapping("/my")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedbacks() {
        String role = getCurrentUserRole();
        if (!"TEACHER".equals(role)) {
            throw new UnauthorizedException("교사만 조회할 수 있습니다.");
        }
        Long teacherId = getCurrentUserId();
        return ResponseEntity.ok(feedbackService.getFeedbacksByTeacher(teacherId));
    }
}
