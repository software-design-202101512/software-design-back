package com.edumanager.teacher;

import com.edumanager.consultation.ConsultationResponse;
import java.util.List;

public record TeacherDashboardResponse(
    long studentCount,
    long monthlyConsultationCount,
    long incompleteFeedbackCount,
    long newNotificationCount,
    List<DashboardNotification> recentNotifications,
    List<ConsultationResponse> todayCounseling
) {
    public record DashboardNotification(
        Long id,
        String text,
        String time,
        String type
    ) {}
}
