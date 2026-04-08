package com.edumanager.service;

import com.edumanager.dto.StudentRecordRequest;
import com.edumanager.dto.StudentRecordResponse;
import com.edumanager.entity.*;
import com.edumanager.exception.UnauthorizedException;
import com.edumanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentRecordService {

    private final StudentRecordRepository studentRecordRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TeacherStudentRepository teacherStudentRepository;
    private final ParentStudentRepository parentStudentRepository;

    public StudentRecordResponse createRecord(Long teacherId, StudentRecordRequest request) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("교사를 찾을 수 없습니다."));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        if (request.category() == RecordCategory.SUBJECT_EVALUATION) {
            if (request.subjectId() == null) {
                throw new UnauthorizedException("교과 평가에는 과목 정보가 필요합니다.");
            }
            boolean isSubjectTeacher = teacherStudentRepository
                    .findByTeacherIdAndSubjectId(teacherId, request.subjectId())
                    .stream()
                    .anyMatch(ts -> !ts.getIsHomeroom());
            if (!isSubjectTeacher) {
                throw new UnauthorizedException("해당 과목의 교과 담당 교사가 아닙니다.");
            }
        } else {
            // BEHAVIOR, ATTENDANCE, SPECIAL_NOTE, OTHER: 담임 확인
            boolean isHomeroom = teacherStudentRepository
                    .findByTeacherIdAndIsHomeroomTrue(teacherId)
                    .stream()
                    .anyMatch(ts -> ts.getStudent().getId().equals(request.studentId()));
            if (!isHomeroom) {
                throw new UnauthorizedException("해당 학생의 담임 교사가 아닙니다.");
            }
        }

        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new RuntimeException("과목을 찾을 수 없습니다."));
        }

        boolean isVisible = request.isVisibleToStudent() != null ? request.isVisibleToStudent() : true;

        StudentRecord record = StudentRecord.builder()
                .student(student)
                .teacher(teacher)
                .subject(subject)
                .category(request.category())
                .content(request.content())
                .isVisibleToStudent(isVisible)
                .build();

        StudentRecord saved = studentRecordRepository.save(record);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentRecordResponse> getRecordsByStudent(Long studentId, Long requesterId, String requesterRole) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        return switch (requesterRole) {
            case "STUDENT" -> {
                if (!student.getUser().getId().equals(requesterId)) {
                    throw new UnauthorizedException("본인의 학생부만 조회할 수 있습니다.");
                }
                yield studentRecordRepository.findByStudentIdAndIsVisibleToStudentTrue(studentId).stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
            }
            case "TEACHER" -> {
                boolean isConnected = teacherStudentRepository.existsByTeacherIdAndStudentId(requesterId, studentId);
                if (isConnected) {
                    yield studentRecordRepository.findByStudentId(studentId).stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList());
                } else {
                    yield studentRecordRepository.findByStudentIdAndIsVisibleToStudentTrue(studentId).stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList());
                }
            }
            case "PARENT" -> {
                if (!parentStudentRepository.existsByParentIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 학생부를 조회할 권한이 없습니다.");
                }
                yield studentRecordRepository.findByStudentIdAndIsVisibleToStudentTrue(studentId).stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
            }
            default -> throw new UnauthorizedException("권한이 없습니다.");
        };
    }

    public StudentRecordResponse updateVisibility(Long recordId, Long teacherId, boolean visible) {
        StudentRecord record = studentRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("학생부 기록을 찾을 수 없습니다."));

        if (!record.getTeacher().getId().equals(teacherId)) {
            throw new UnauthorizedException("본인이 작성한 학생부 기록만 수정할 수 있습니다.");
        }

        record.updateVisibility(visible);
        StudentRecord saved = studentRecordRepository.save(record);
        return toResponse(saved);
    }

    private StudentRecordResponse toResponse(StudentRecord record) {
        return new StudentRecordResponse(
                record.getId(),
                record.getStudent().getId(),
                record.getStudent().getUser().getName(),
                record.getTeacher().getName(),
                record.getSubject() != null ? record.getSubject().getId() : null,
                record.getSubject() != null ? record.getSubject().getName() : null,
                record.getCategory(),
                record.getContent(),
                record.getIsVisibleToStudent(),
                record.getCreatedAt()
        );
    }
}
