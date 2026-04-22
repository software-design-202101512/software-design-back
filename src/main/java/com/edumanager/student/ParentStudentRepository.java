package com.edumanager.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    List<ParentStudent> findByParentProfileUserId(Long userId);
    List<ParentStudent> findByStudentId(Long studentId);
    boolean existsByParentProfileUserIdAndStudentId(Long userId, Long studentId);
}
