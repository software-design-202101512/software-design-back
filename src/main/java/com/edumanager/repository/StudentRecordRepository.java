package com.edumanager.repository;

import com.edumanager.entity.RecordCategory;
import com.edumanager.entity.StudentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRecordRepository extends JpaRepository<StudentRecord, Long> {
    List<StudentRecord> findByStudentId(Long studentId);
    List<StudentRecord> findByStudentIdAndCategory(Long studentId, RecordCategory category);
    List<StudentRecord> findByStudentIdAndIsVisibleToStudentTrue(Long studentId);
    List<StudentRecord> findByTeacherId(Long teacherId);
}
