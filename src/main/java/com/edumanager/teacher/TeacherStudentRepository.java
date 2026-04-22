package com.edumanager.teacher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherStudentRepository extends JpaRepository<TeacherStudent, Long> {
    long countByTeacherProfileUserId(Long userId);
    List<TeacherStudent> findByTeacherProfileUserId(Long userId);
    List<TeacherStudent> findByStudentId(Long studentId);
    List<TeacherStudent> findByTeacherProfileUserIdAndStudentId(Long userId, Long studentId);
    boolean existsByTeacherProfileUserIdAndStudentId(Long userId, Long studentId);
    List<TeacherStudent> findByTeacherProfileUserIdAndIsHomeroomTrue(Long userId);
    List<TeacherStudent> findByTeacherProfileUserIdAndSubjectId(Long userId, Long subjectId);
}
