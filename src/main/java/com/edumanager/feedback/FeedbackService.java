package com.edumanager.feedback;

import com.edumanager.exception.UnauthorizedException;
import com.edumanager.student.Student;
import com.edumanager.student.StudentRepository;
import com.edumanager.student.ParentStudentRepository;
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
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherStudentRepository teacherStudentRepository;
    private final ParentStudentRepository parentStudentRepository;

    /**
     * 교사가 학생에 대한 피드백을 작성합니다.
     * - 담당 학생(teacher_student_mappings)인지 권한 확인
     */
    public FeedbackResponse createFeedback(Long teacherUserId, FeedbackRequest request) {
        TeacherProfile teacher = teacherProfileRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("교사 프로필을 찾을 수 없습니다."));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        // 권한 체크: 담당 학생이어야 함
        if (!teacherStudentRepository.existsByTeacherProfileUserIdAndStudentId(teacherUserId, request.studentId())) {
            throw new UnauthorizedException("해당 학생에 대한 피드백을 작성할 권한이 없습니다.");
        }

        Feedback feedback = Feedback.builder()
                .student(student)
                .teacher(teacher)
                .content(request.content())
                .isPublicToStudentParent(request.isPublicToStudentParent() != null ? request.isPublicToStudentParent() : true)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    /**
     * 학생별 피드백 조회
     * - TEACHER: 전체 피드백 조회
     * - STUDENT/PARENT: 공개(isPublicToStudentParent=true) 피드백만 조회
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByStudent(Long studentId, Long requesterId, String requesterRole) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        List<Feedback> feedbacks = feedbackRepository.findByStudentId(studentId);

        switch (requesterRole) {
            case "TEACHER" -> {
                if (!teacherStudentRepository.existsByTeacherProfileUserIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 피드백을 조회할 권한이 없습니다.");
                }
            }
            case "STUDENT" -> {
                if (!student.getUser().getId().equals(requesterId)) {
                    throw new UnauthorizedException("본인의 피드백만 조회할 수 있습니다.");
                }
                // 학생은 공개 피드백만 조회 가능
                feedbacks = feedbacks.stream()
                        .filter(Feedback::getIsPublicToStudentParent)
                        .collect(Collectors.toList());
            }
            case "PARENT" -> {
                if (!parentStudentRepository.existsByParentProfileUserIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 피드백을 조회할 권한이 없습니다.");
                }
                // 학부모도 공개 피드백만 조회 가능
                feedbacks = feedbacks.stream()
                        .filter(Feedback::getIsPublicToStudentParent)
                        .collect(Collectors.toList());
            }
            default -> throw new UnauthorizedException("권한이 없습니다.");
        }

        return feedbacks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 교사가 작성한 전체 피드백 목록 */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByTeacher(Long teacherUserId) {
        return feedbackRepository.findByTeacherUserId(teacherUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FeedbackResponse toResponse(Feedback f) {
        return new FeedbackResponse(
                f.getId(),
                f.getStudent().getId(),
                f.getStudent().getUser().getName(),
                f.getTeacher().getUser().getName(),
                f.getContent(),
                f.getIsPublicToStudentParent(),
                f.getCreatedAt()
        );
    }
}
