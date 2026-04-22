package com.edumanager.student;

import com.edumanager.teacher.TeacherStudent;
import com.edumanager.teacher.TeacherStudentRepository;
import com.edumanager.user.Role;
import com.edumanager.user.User;
import com.edumanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TeacherStudentRepository teacherStudentRepository;

    /**
     * 학생 본인 프로필 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyStudentProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));
        return ResponseEntity.ok(Map.of(
                "studentId", student.getId(),
                "name", student.getUser().getName(),
                "grade", student.getGrade(),
                "classNum", student.getClassNum(),
                "studentNum", student.getStudentNum()
        ));
    }

    /**
     * 교사의 담당 학생 목록 조회 (TeacherStudent 매핑 기반)
     * - 교사: 자신에게 매핑된 학생 목록
     * - 관리자: 전체 학생 목록
     */
    @GetMapping("/my-students")
    public ResponseEntity<List<Map<String, Object>>> getMyStudents() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Map<String, Object>> result;

        if (user.getRole() == Role.ADMIN) {
            result = studentRepository.findAllApprovedWithUser().stream()
                    .map(this::toStudentMap)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.TEACHER) {
            List<TeacherStudent> mappings = teacherStudentRepository.findByTeacherProfileUserId(user.getId());
            result = mappings.stream()
                    .map(TeacherStudent::getStudent)
                    .distinct()
                    .map(this::toStudentMap)
                    .collect(Collectors.toList());
        } else {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toStudentMap(Student s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("name", s.getUser().getName());
        map.put("grade", s.getGrade());
        map.put("classNum", s.getClassNum());
        map.put("studentNum", s.getStudentNum());
        return map;
    }
}
