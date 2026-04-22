package com.edumanager.user;

import com.edumanager.preregistration.PreRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── JSON 기반 일괄 등록 ──

    @PostMapping("/bulk-register/teachers")
    public ResponseEntity<String> registerTeachers(@RequestBody List<TeacherRegistrationRequest> requests) {
        adminService.registerTeachersBulk(requests);
        return ResponseEntity.ok("교사 " + requests.size() + "명 등록 완료");
    }

    @PostMapping("/bulk-register/students")
    public ResponseEntity<String> registerStudents(@RequestBody List<StudentRegistrationRequest> requests) {
        adminService.registerStudentsBulk(requests);
        return ResponseEntity.ok("학생 " + requests.size() + "명 등록 완료");
    }

    // ── 엑셀 파일 기반 일괄 등록 ──

    @PostMapping("/upload/teachers")
    public ResponseEntity<Map<String, Object>> uploadTeacherExcel(@RequestParam("file") MultipartFile file) throws IOException {
        validateExcelFile(file);
        int count = adminService.registerTeachersFromExcel(file);
        return ResponseEntity.ok(Map.of(
                "message", "교사 엑셀 업로드 완료",
                "registeredCount", count
        ));
    }

    @PostMapping("/upload/students")
    public ResponseEntity<Map<String, Object>> uploadStudentExcel(@RequestParam("file") MultipartFile file) throws IOException {
        validateExcelFile(file);
        int count = adminService.registerStudentsFromExcel(file);
        return ResponseEntity.ok(Map.of(
                "message", "학생 엑셀 업로드 완료",
                "registeredCount", count
        ));
    }

    // ── 사전등록 목록 조회 ──

    @GetMapping("/pre-registrations")
    public ResponseEntity<List<PreRegistration>> getPreRegistrations() {
        return ResponseEntity.ok(adminService.getPreRegistrations());
    }

    @GetMapping("/pre-registrations/waiting")
    public ResponseEntity<List<PreRegistration>> getWaitingPreRegistrations() {
        return ResponseEntity.ok(adminService.getWaitingPreRegistrations());
    }

    // ── 가입 승인/거절 ──

    @GetMapping("/pending-users")
    public ResponseEntity<List<PendingUserResponse>> getPendingUsers() {
        List<User> pendingUsers = adminService.getPendingUsers();
        List<PendingUserResponse> response = pendingUsers.stream()
                .map(u -> new PendingUserResponse(u.getId(), u.getEmail(), u.getName(), u.getRole(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<String> approveUser(@PathVariable Long userId) {
        adminService.approveUser(userId);
        return ResponseEntity.ok("사용자 승인 완료");
    }

    @PatchMapping("/users/{userId}/reject")
    public ResponseEntity<String> rejectUser(@PathVariable Long userId) {
        adminService.rejectUser(userId);
        return ResponseEntity.ok("사용자 거절 완료");
    }

    private void validateExcelFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw new IllegalArgumentException("xlsx 형식의 엑셀 파일만 지원합니다.");
        }
    }
}
