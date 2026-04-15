package com.edumanager.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    List<ParentStudent> findByParentId(Long parentId);
    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);
}
