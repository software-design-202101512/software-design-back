package com.edumanager.grade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    /** N+1 방지: student, subject, teacherProfile, user를 한 번에 JOIN FETCH */
    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.student s JOIN FETCH s.user " +
           "JOIN FETCH g.subject " +
           "JOIN FETCH g.teacherProfile tp JOIN FETCH tp.user " +
           "WHERE g.student.id = :studentId")
    List<Grade> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.student s JOIN FETCH s.user " +
           "JOIN FETCH g.subject " +
           "JOIN FETCH g.teacherProfile tp JOIN FETCH tp.user " +
           "WHERE g.student.id = :studentId AND g.year = :year AND g.semester = :semester")
    List<Grade> findByStudentIdAndYearAndSemesterWithDetails(
            @Param("studentId") Long studentId,
            @Param("year") int year,
            @Param("semester") int semester);

    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByStudentIdAndYearAndSemester(Long studentId, int year, int semester);
    List<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

    /**
     * 상대평가용: 같은 과목+학년+학기의 모든 성적을 점수 내림차순으로 조회
     * → 석차백분율 기반 9등급 산출에 사용
     */
    @Query("SELECT g FROM Grade g " +
           "WHERE g.subject.id = :subjectId AND g.year = :year AND g.semester = :semester " +
           "ORDER BY g.score DESC")
    List<Grade> findBySubjectIdAndYearAndSemesterOrderByScoreDesc(
            @Param("subjectId") Long subjectId,
            @Param("year") int year,
            @Param("semester") int semester);
}
