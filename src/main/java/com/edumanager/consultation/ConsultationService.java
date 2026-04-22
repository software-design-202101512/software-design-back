package com.edumanager.consultation;

import com.edumanager.exception.UnauthorizedException;
import com.edumanager.student.Student;
import com.edumanager.student.StudentRepository;
import com.edumanager.teacher.TeacherProfile;
import com.edumanager.teacher.TeacherProfileRepository;
import com.edumanager.teacher.TeacherStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final StudentRepository studentRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherStudentRepository teacherStudentRepository;

    /**
     * 교사가 상담 기록을 작성합니다.
     * - 담당 학생(teacher_student_mappings)인지 권한 확인
     */
    public ConsultationResponse createConsultation(Long teacherUserId, ConsultationRequest request) {
        TeacherProfile teacher = teacherProfileRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("교사 프로필을 찾을 수 없습니다."));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        // 권한 체크: 담당 학생이어야 함
        if (!teacherStudentRepository.existsByTeacherProfileUserIdAndStudentId(teacherUserId, request.studentId())) {
            throw new UnauthorizedException("해당 학생의 상담 기록을 작성할 권한이 없습니다.");
        }

        Consultation consultation = Consultation.builder()
                .student(student)
                .teacher(teacher)
                .consultationDate(request.consultationDate())
                .mainContent(request.mainContent())
                .nextPlan(request.nextPlan())
                .isSharedWithTeachers(request.isSharedWithTeachers() != null ? request.isSharedWithTeachers() : false)
                .build();

        Consultation saved = consultationRepository.save(consultation);
        return toResponse(saved);
    }

    /**
     * 학생별 상담 기록 조회
     * - TEACHER (본인 작성): 모든 기록 조회 가능
     * - TEACHER (다른 교사): isSharedWithTeachers=true 인 기록만 조회
     * - STUDENT/PARENT: isSharedWithTeachers와 관계없이 모든 기록 조회
     */
    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByStudent(Long studentId, Long requesterUserId, String requesterRole) {
        if ("TEACHER".equals(requesterRole)) {
            // 본인이 작성한 기록 + 다른 교사의 공개 기록
            List<Consultation> all = consultationRepository.findByStudentId(studentId);
            return all.stream()
                    .filter(c -> c.getTeacher().getUser().getId().equals(requesterUserId)
                            || Boolean.TRUE.equals(c.getIsSharedWithTeachers()))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        // STUDENT, PARENT → 전체 조회 (본인 관련)
        return consultationRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 담당 학생들의 다른 교사가 공유한 상담 기록 조회
     * - 본인이 담당하는 학생들 중 다른 교사가 isSharedWithTeachers=true 로 작성한 기록
     */
    @Transactional(readOnly = true)
    public List<ConsultationResponse> getSharedConsultations(Long teacherUserId) {
        List<Long> myStudentIds = teacherStudentRepository.findByTeacherProfileUserId(teacherUserId)
                .stream()
                .map(ts -> ts.getStudent().getId())
                .collect(Collectors.toList());

        if (myStudentIds.isEmpty()) return List.of();

        return consultationRepository
                .findByStudentIdInAndIsSharedWithTeachersTrueAndTeacherUserIdNot(myStudentIds, teacherUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 교사가 작성한 전체 상담 목록 */
    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByTeacher(Long teacherUserId) {
        return consultationRepository.findByTeacherUserId(teacherUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ConsultationResponse toResponse(Consultation c) {
        return new ConsultationResponse(
                c.getId(),
                c.getStudent().getId(),
                c.getStudent().getUser().getName(),
                c.getTeacher().getUser().getName(),
                c.getConsultationDate(),
                c.getMainContent(),
                c.getNextPlan(),
                c.getIsSharedWithTeachers(),
                c.getCreatedAt()
        );
    }
}
