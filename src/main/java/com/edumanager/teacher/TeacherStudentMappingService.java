package com.edumanager.teacher;

import com.edumanager.student.Student;
import com.edumanager.student.StudentRepository;
import com.edumanager.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 교사-학생 자동 매핑 서비스
 *
 * 매핑 규칙:
 * 1. 담임 교사 → 같은 학년·반의 모든 학생 (isHomeroom=true, subject=null)
 * 2. 교과 교사 → 같은 학년의 모든 학생 (isHomeroom=false, subject=교사의 담당과목)
 *
 * 트리거 시점:
 * - 교사 승인 시: 해당 교사 ↔ 기존 학생들 매핑
 * - 학생 승인 시: 해당 학생 ↔ 기존 교사들 매핑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherStudentMappingService {

    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherStudentRepository teacherStudentRepository;
    private final StudentRepository studentRepository;

    /**
     * 교사가 승인되었을 때 호출 — 기존 학생들과 매핑 생성
     */
    @Transactional
    public void mapTeacherToExistingStudents(User teacherUser) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(teacherUser.getId())
                .orElse(null);
        if (profile == null) {
            log.warn("교사 프로필 없음: userId={}", teacherUser.getId());
            return;
        }

        // 1) 담임이면 → 같은 학년·반 학생 전원 매핑
        if (Boolean.TRUE.equals(profile.getIsHomeroom())
                && profile.getHomeroomGrade() != null
                && profile.getHomeroomClassNum() != null) {
            List<Student> classStudents = studentRepository.findByGradeAndClassNum(
                    profile.getHomeroomGrade(), profile.getHomeroomClassNum());
            for (Student student : classStudents) {
                createMappingIfNotExists(profile, student, true, null);
            }
            log.info("담임 매핑 완료: 교사={}, {}학년 {}반, 학생 {}명",
                    teacherUser.getName(), profile.getHomeroomGrade(),
                    profile.getHomeroomClassNum(), classStudents.size());
        }

        // 2) 담당 과목이 있으면 → 같은 학년 학생 전원에게 교과 매핑
        //    (담임 학년 기준, 담임이 아니면 전 학년 학생에게 매핑)
        if (profile.getSubject() != null) {
            List<Student> subjectStudents;
            if (Boolean.TRUE.equals(profile.getIsHomeroom()) && profile.getHomeroomGrade() != null) {
                subjectStudents = studentRepository.findByGrade(profile.getHomeroomGrade());
            } else {
                // 비담임 교과 교사는 전체 학생에게 매핑
                subjectStudents = studentRepository.findAll();
            }
            for (Student student : subjectStudents) {
                createMappingIfNotExists(profile, student, false, profile.getSubject());
            }
            log.info("교과 매핑 완료: 교사={}, 과목={}, 학생 {}명",
                    teacherUser.getName(), profile.getSubject().getName(), subjectStudents.size());
        }
    }

    /**
     * 학생이 승인되었을 때 호출 — 기존 교사들과 매핑 생성
     */
    @Transactional
    public void mapStudentToExistingTeachers(User studentUser) {
        Student student = studentRepository.findByUserId(studentUser.getId()).orElse(null);
        if (student == null) {
            log.warn("학생 프로필 없음: userId={}", studentUser.getId());
            return;
        }

        List<TeacherProfile> allTeachers = teacherProfileRepository.findAll();
        for (TeacherProfile teacher : allTeachers) {
            // 담임 매핑: 학생의 학년·반이 교사의 담임 학년·반과 일치
            if (Boolean.TRUE.equals(teacher.getIsHomeroom())
                    && student.getGrade().equals(teacher.getHomeroomGrade())
                    && student.getClassNum().equals(teacher.getHomeroomClassNum())) {
                createMappingIfNotExists(teacher, student, true, null);
            }

            // 교과 매핑: 교사의 담당 과목이 있고, 같은 학년이거나 비담임 교과 교사
            if (teacher.getSubject() != null) {
                boolean sameGrade = Boolean.TRUE.equals(teacher.getIsHomeroom())
                        && teacher.getHomeroomGrade() != null
                        && student.getGrade().equals(teacher.getHomeroomGrade());
                boolean nonHomeroomTeacher = !Boolean.TRUE.equals(teacher.getIsHomeroom());

                if (sameGrade || nonHomeroomTeacher) {
                    createMappingIfNotExists(teacher, student, false, teacher.getSubject());
                }
            }
        }
        log.info("학생 매핑 완료: 학생={}, {}학년 {}반",
                studentUser.getName(), student.getGrade(), student.getClassNum());
    }

    private void createMappingIfNotExists(TeacherProfile teacher, Student student,
                                          boolean isHomeroom, com.edumanager.subject.Subject subject) {
        // 같은 교사-학생이지만 다른 유형(담임/교과)일 수 있으므로 상세 체크
        List<TeacherStudent> existing = teacherStudentRepository
                .findByTeacherProfileUserIdAndStudentId(teacher.getUser().getId(), student.getId());
        boolean duplicateFound = existing.stream().anyMatch(m ->
                m.getIsHomeroom().equals(isHomeroom)
                && ((m.getSubject() == null && subject == null)
                    || (m.getSubject() != null && subject != null
                        && m.getSubject().getId().equals(subject.getId()))));
        if (duplicateFound) return;

        teacherStudentRepository.save(TeacherStudent.builder()
                .teacherProfile(teacher)
                .student(student)
                .isHomeroom(isHomeroom)
                .subject(subject)
                .build());
    }
}
