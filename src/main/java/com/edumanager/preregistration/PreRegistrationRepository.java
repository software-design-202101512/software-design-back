package com.edumanager.preregistration;

import com.edumanager.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreRegistrationRepository extends JpaRepository<PreRegistration, Long> {

    // 교사 매칭: 이름 + 이메일
    Optional<PreRegistration> findByRoleAndNameAndEmailAndStatus(
            Role role, String name, String email, PreRegistrationStatus status);

    // 학생 매칭: 이름 + 학년 + 반 + 번호
    Optional<PreRegistration> findByRoleAndNameAndGradeAndClassNumAndStudentNumAndStatus(
            Role role, String name, String grade, String classNum, String studentNum, PreRegistrationStatus status);

    List<PreRegistration> findByStatus(PreRegistrationStatus status);

    List<PreRegistration> findAllByOrderByCreatedAtDesc();
}
