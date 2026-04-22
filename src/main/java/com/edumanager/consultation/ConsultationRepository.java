package com.edumanager.consultation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByStudentId(Long studentId);
    List<Consultation> findByStudentIdAndIsSharedWithTeachersTrue(Long studentId);
    List<Consultation> findByTeacherUserId(Long userId);
    List<Consultation> findByStudentIdInAndIsSharedWithTeachersTrueAndTeacherUserIdNot(List<Long> studentIds, Long teacherUserId);
    List<Consultation> findByConsultationDateAndTeacherUserId(LocalDate date, Long teacherUserId);
    long countByTeacherUserIdAndConsultationDateBetween(Long teacherUserId, LocalDate start, LocalDate end);
}
