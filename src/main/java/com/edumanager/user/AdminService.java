package com.edumanager.user;

import com.edumanager.preregistration.PreRegistration;
import com.edumanager.preregistration.PreRegistrationRepository;
import com.edumanager.preregistration.PreRegistrationStatus;
import com.edumanager.teacher.TeacherStudentMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PreRegistrationRepository preRegistrationRepository;
    private final ExcelParsingService excelParsingService;
    private final TeacherStudentMappingService teacherStudentMappingService;

    // ── JSON 기반 사전등록 ──

    @Transactional
    public void registerTeachersBulk(List<TeacherRegistrationRequest> requests) {
        for (TeacherRegistrationRequest req : requests) {
            preRegistrationRepository.save(PreRegistration.builder()
                    .role(Role.TEACHER)
                    .name(req.getName())
                    .email(req.getEmail())
                    .subjectId(req.getSubjectId())
                    .isHomeroom(req.getIsHomeroom())
                    .homeroomGrade(req.getHomeroomGrade())
                    .homeroomClassNum(req.getHomeroomClassNum())
                    .status(PreRegistrationStatus.WAITING)
                    .build());
        }
    }

    @Transactional
    public void registerStudentsBulk(List<StudentRegistrationRequest> requests) {
        for (StudentRegistrationRequest req : requests) {
            preRegistrationRepository.save(PreRegistration.builder()
                    .role(Role.STUDENT)
                    .name(req.getName())
                    .email(req.getEmail())
                    .grade(req.getGrade())
                    .classNum(req.getClassNum())
                    .studentNum(req.getStudentNum())
                    .gender(req.getGender())
                    .status(PreRegistrationStatus.WAITING)
                    .build());
        }
    }

    // ── 엑셀 기반 사전등록 ──

    @Transactional
    public int registerTeachersFromExcel(MultipartFile file) throws IOException {
        List<TeacherRegistrationRequest> requests = excelParsingService.parseTeacherExcel(file);
        registerTeachersBulk(requests);
        return requests.size();
    }

    @Transactional
    public int registerStudentsFromExcel(MultipartFile file) throws IOException {
        List<StudentRegistrationRequest> requests = excelParsingService.parseStudentExcel(file);
        registerStudentsBulk(requests);
        return requests.size();
    }

    // ── 사전등록 목록 조회 ──

    @Transactional(readOnly = true)
    public List<PreRegistration> getPreRegistrations() {
        return preRegistrationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<PreRegistration> getWaitingPreRegistrations() {
        return preRegistrationRepository.findByStatus(PreRegistrationStatus.WAITING);
    }

    // ── 가입 승인/거절 ──

    @Transactional(readOnly = true)
    public List<User> getPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING);
    }

    @Transactional
    public void approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 사용자만 승인할 수 있습니다.");
        }
        user.updateStatus(UserStatus.APPROVED);

        // 승인 후 교사-학생 자동 매핑
        if (user.getRole() == Role.TEACHER) {
            teacherStudentMappingService.mapTeacherToExistingStudents(user);
        } else if (user.getRole() == Role.STUDENT) {
            teacherStudentMappingService.mapStudentToExistingTeachers(user);
        }
    }

    @Transactional
    public void rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 사용자만 거절할 수 있습니다.");
        }
        user.updateStatus(UserStatus.REJECTED);
    }
}
