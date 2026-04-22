package com.edumanager.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByInvitationCode(String invitationCode);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByGradeAndClassNum(String grade, String classNum);
    List<Student> findByGrade(String grade);

    @Query("SELECT s FROM Student s JOIN FETCH s.user WHERE s.user.status = 'APPROVED'")
    List<Student> findAllApprovedWithUser();
}
