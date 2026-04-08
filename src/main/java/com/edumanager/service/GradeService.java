package com.edumanager.service;

import com.edumanager.dto.GradeRequest;
import com.edumanager.dto.GradeResponse;
import com.edumanager.dto.StudentGradeSummary;
import com.edumanager.entity.Grade;
import com.edumanager.entity.Student;
import com.edumanager.entity.Subject;
import com.edumanager.entity.User;
import com.edumanager.exception.UnauthorizedException;
import com.edumanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TeacherStudentRepository teacherStudentRepository;
    private final ParentStudentRepository parentStudentRepository;

    public GradeResponse createGrade(Long teacherId, GradeRequest request) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("교사를 찾을 수 없습니다."));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new RuntimeException("과목을 찾을 수 없습니다."));

        if (!teacherStudentRepository.existsByTeacherIdAndStudentId(teacherId, request.studentId())) {
            throw new UnauthorizedException("해당 학생의 성적을 입력할 권한이 없습니다.");
        }

        Grade grade = Grade.builder()
                .student(student)
                .subject(subject)
                .teacher(teacher)
                .year(request.year())
                .semester(request.semester())
                .score(request.score())
                .rank(request.rank())
                .build();

        Grade saved = gradeRepository.save(grade);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByStudent(Long studentId, Long requesterId, String requesterRole) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        switch (requesterRole) {
            case "TEACHER" -> {
                if (!teacherStudentRepository.existsByTeacherIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 성적을 조회할 권한이 없습니다.");
                }
            }
            case "STUDENT" -> {
                if (!student.getUser().getId().equals(requesterId)) {
                    throw new UnauthorizedException("본인의 성적만 조회할 수 있습니다.");
                }
            }
            case "PARENT" -> {
                if (!parentStudentRepository.existsByParentIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 성적을 조회할 권한이 없습니다.");
                }
            }
            default -> throw new UnauthorizedException("권한이 없습니다.");
        }

        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentGradeSummary getStudentGradeSummary(Long studentId, int year, int semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        List<Grade> grades = gradeRepository.findByStudentIdAndYearAndSemester(studentId, year, semester);
        List<GradeResponse> gradeResponses = grades.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        BigDecimal totalScore = grades.stream()
                .map(Grade::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageScore = grades.isEmpty()
                ? BigDecimal.ZERO
                : totalScore.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);

        return new StudentGradeSummary(
                student.getId(),
                student.getUser().getName(),
                year,
                semester,
                gradeResponses,
                averageScore,
                totalScore
        );
    }

    private GradeResponse toResponse(Grade grade) {
        return new GradeResponse(
                grade.getId(),
                grade.getStudent().getId(),
                grade.getStudent().getUser().getName(),
                grade.getSubject().getId(),
                grade.getSubject().getName(),
                grade.getYear(),
                grade.getSemester(),
                grade.getScore(),
                grade.getRank(),
                grade.getTeacher().getName(),
                grade.getCreatedAt()
        );
    }
}
