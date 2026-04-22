package com.edumanager.record;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRecordRepository extends JpaRepository<StudentRecord, Long> {

    /** N+1 방지: student, teacherProfile, subject, user를 한 번에 JOIN FETCH */
    @Query("SELECT r FROM StudentRecord r " +
           "JOIN FETCH r.student s JOIN FETCH s.user " +
           "JOIN FETCH r.teacherProfile tp JOIN FETCH tp.user " +
           "LEFT JOIN FETCH r.subject " +
           "WHERE r.student.id = :studentId")
    List<StudentRecord> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    List<StudentRecord> findByStudentId(Long studentId);
    List<StudentRecord> findByStudentIdAndCategory(Long studentId, RecordCategory category);
    List<StudentRecord> findByStudentIdAndIsVisibleToStudentTrue(Long studentId);
    List<StudentRecord> findByTeacherProfileId(Long teacherProfileId);
}
