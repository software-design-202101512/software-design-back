package com.edumanager.controller;

import com.edumanager.entity.Student;
import com.edumanager.repository.StudentRepository;
import com.edumanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyStudentProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));
        return ResponseEntity.ok(Map.of(
                "studentId", student.getId(),
                "grade", student.getGrade(),
                "classNum", student.getClassNum(),
                "studentNum", student.getStudentNum()
        ));
    }
}
