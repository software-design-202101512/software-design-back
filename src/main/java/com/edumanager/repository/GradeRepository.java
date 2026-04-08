package com.edumanager.repository;

import com.edumanager.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByStudentIdAndYearAndSemester(Long studentId, int year, int semester);
    List<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
}
