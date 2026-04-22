package com.edumanager.teacher;

import com.edumanager.consultation.Consultation;
import com.edumanager.consultation.ConsultationRepository;
import com.edumanager.consultation.ConsultationResponse;
import com.edumanager.notification.Notification;
import com.edumanager.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherDashboardService {

    private final TeacherStudentRepository teacherStudentRepository;
    private final ConsultationRepository consultationRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public TeacherDashboardResponse getDashboardData(Long teacherUserId) {
        // 1. 담당 학생 수
        long studentCount = teacherStudentRepository.countByTeacherProfileUserId(teacherUserId);

        // 2. 이번 달 상담 건수
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        long monthlyConsultationCount = consultationRepository.countByTeacherUserIdAndConsultationDateBetween(
                teacherUserId, startOfMonth, endOfMonth);

        // 3. 미완료 피드백 (사용자 요청에 따라 나중에 처리, 현재 0으로 설정)
        long incompleteFeedbackCount = 0;

        // 4. 새 알림 수
        long newNotificationCount = notificationRepository.countByReceiverIdAndIsReadFalse(teacherUserId);

        // 5. 최근 알림 (최신 5개)
        List<TeacherDashboardResponse.DashboardNotification> recentNotifications = notificationRepository
                .findTop5ByReceiverIdOrderByCreatedAtDesc(teacherUserId)
                .stream()
                .map(n -> new TeacherDashboardResponse.DashboardNotification(
                        n.getId(),
                        n.getMessage(),
                        formatTime(n.getCreatedAt()),
                        "info"
                ))
                .collect(Collectors.toList());

        // 6. 오늘 상담 일정
        List<ConsultationResponse> todayCounseling = consultationRepository
                .findByConsultationDateAndTeacherUserId(now, teacherUserId)
                .stream()
                .map(this::toConsultationResponse)
                .collect(Collectors.toList());

        return new TeacherDashboardResponse(
                studentCount,
                monthlyConsultationCount,
                incompleteFeedbackCount,
                newNotificationCount,
                recentNotifications,
                todayCounseling
        );
    }

    private String formatTime(LocalDateTime createdAt) {
        // 단순하게 구현 (실제로는 방금 전, 몇 분 전 등으로 변환 가능)
        return createdAt.toLocalTime().toString();
    }

    private ConsultationResponse toConsultationResponse(Consultation c) {
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
