package com.edumanager.record;

import com.edumanager.exception.UnauthorizedException;
import com.edumanager.student.Student;
import com.edumanager.student.StudentRepository;
import com.edumanager.student.ParentStudentRepository;
import com.edumanager.subject.Subject;
import com.edumanager.subject.SubjectRepository;
import com.edumanager.teacher.TeacherProfile;
import com.edumanager.teacher.TeacherProfileRepository;
import com.edumanager.teacher.TeacherStudentRepository;
import com.edumanager.user.User;
import com.edumanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentRecordService {

    private final StudentRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherStudentRepository teacherStudentRepository;
    private final ParentStudentRepository parentStudentRepository;

    public StudentRecordResponse createRecord(Long teacherUserId, StudentRecordRequest request) {
        TeacherProfile teacherProfile = teacherProfileRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("교사 프로필을 찾을 수 없습니다."));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new RuntimeException("과목을 찾을 수 없습니다."));
        }

        // 권한 체크: 담임이거나 해당 과목 담당 교사여야 함
        if (!teacherStudentRepository.existsByTeacherProfileUserIdAndStudentId(teacherUserId, request.studentId())) {
            throw new UnauthorizedException("해당 학생의 기록을 입력할 권한이 없습니다.");
        }

        StudentRecord record = StudentRecord.builder()
                .student(student)
                .teacherProfile(teacherProfile)
                .subject(subject)
                .category(request.category())
                .content(request.content())
                .isVisibleToStudent(request.isVisibleToStudent())
                .build();

        StudentRecord saved = recordRepository.save(record);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentRecordResponse> getRecordsByStudent(Long studentId, Long requesterId, String requesterRole) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        // JOIN FETCH 쿼리로 연관 엔티티를 한 번에 로딩 → N+1 방지
        List<StudentRecord> records = recordRepository.findByStudentIdWithDetails(studentId);

        // 역할에 따른 필터링 및 권한 체크
        switch (requesterRole) {
            case "TEACHER" -> {
                if (!teacherStudentRepository.existsByTeacherProfileUserIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 기록을 조회할 권한이 없습니다.");
                }
            }
            case "STUDENT" -> {
                if (!student.getUser().getId().equals(requesterId)) {
                    throw new UnauthorizedException("본인의 기록만 조회할 수 있습니다.");
                }
                records = records.stream().filter(StudentRecord::getIsVisibleToStudent).collect(Collectors.toList());
            }
            case "PARENT" -> {
                if (!parentStudentRepository.existsByParentProfileUserIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 기록을 조회할 권한이 없습니다.");
                }
                records = records.stream().filter(StudentRecord::getIsVisibleToStudent).collect(Collectors.toList());
            }
            default -> throw new UnauthorizedException("권한이 없습니다.");
        }

        return records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public StudentRecordResponse updateRecord(Long recordId, Long teacherUserId, StudentRecordRequest request) {
        StudentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("기록을 찾을 수 없습니다."));

        if (!record.getTeacherProfile().getUser().getId().equals(teacherUserId)) {
            throw new UnauthorizedException("본인이 입력한 기록만 수정할 수 있습니다.");
        }

        record.updateContent(request.content());
        record.updateVisibility(request.isVisibleToStudent());
        
        StudentRecord saved = recordRepository.save(record);
        return toResponse(saved);
    }

    public void updateVisibility(Long recordId, Long teacherUserId, boolean visible) {
        StudentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("기록을 찾을 수 없습니다."));

        if (!record.getTeacherProfile().getUser().getId().equals(teacherUserId)) {
            throw new UnauthorizedException("본인이 입력한 기록의 공개 여부만 변경할 수 있습니다.");
        }

        record.updateVisibility(visible);
        recordRepository.save(record);
    }

    public void deleteRecord(Long recordId, Long teacherUserId) {
        StudentRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("기록을 찾을 수 없습니다."));

        if (!record.getTeacherProfile().getUser().getId().equals(teacherUserId)) {
            throw new UnauthorizedException("본인이 입력한 기록만 삭제할 수 있습니다.");
        }

        recordRepository.delete(record);
    }

    private StudentRecordResponse toResponse(StudentRecord record) {
        return new StudentRecordResponse(
                record.getId(),
                record.getStudent().getId(),
                record.getStudent().getUser().getName(),
                record.getTeacherProfile().getUser().getName(),
                record.getSubject() != null ? record.getSubject().getId() : null,
                record.getSubject() != null ? record.getSubject().getName() : "공통",
                record.getCategory().name(),
                record.getContent(),
                record.getIsVisibleToStudent(),
                record.getCreatedAt()
        );
    }
}
