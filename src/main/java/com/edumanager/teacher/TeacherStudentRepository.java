package com.edumanager.teacher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherStudentRepository extends JpaRepository<TeacherStudent, Long> {
    List<TeacherStudent> findByTeacherId(Long teacherId);
    List<TeacherStudent> findByStudentId(Long studentId);
    List<TeacherStudent> findByTeacherIdAndStudentId(Long teacherId, Long studentId);
    boolean existsByTeacherIdAndStudentId(Long teacherId, Long studentId);
    List<TeacherStudent> findByTeacherIdAndIsHomeroomTrue(Long teacherId);
    List<TeacherStudent> findByTeacherIdAndSubjectId(Long teacherId, Long subjectId);
}
