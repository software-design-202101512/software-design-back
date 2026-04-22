package com.edumanager.grade;

import com.edumanager.student.Student;
import com.edumanager.subject.Subject;
import com.edumanager.teacher.TeacherProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_profile_id")
    private TeacherProfile teacherProfile;

    @Column(name = "grade_year")
    private int year;

    @Column(name = "grade_semester")
    private int semester;

    private BigDecimal score;

    @Column(name = "grade_rank")
    private String rank;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void updateScore(BigDecimal score, String rank) {
        this.score = score;
        this.rank = rank;
    }

    public void updateRank(String rank) {
        this.rank = rank;
    }

    /**
     * 한국 수능 9등급제 — 상대평가(석차백분율) 기준
     *
     * 같은 과목·학년·학기를 수강하는 전체 학생 중
     * 해당 학생이 상위 몇 %에 위치하는지(석차백분율)에 따라 등급을 부여합니다.
     *
     *   1등급: 상위  0% ~ 4%   (누적  4%)
     *   2등급: 상위  4% ~ 11%  (누적 11%)
     *   3등급: 상위 11% ~ 23%  (누적 23%)
     *   4등급: 상위 23% ~ 40%  (누적 40%)
     *   5등급: 상위 40% ~ 60%  (누적 60%)
     *   6등급: 상위 60% ~ 77%  (누적 77%)
     *   7등급: 상위 77% ~ 89%  (누적 89%)
     *   8등급: 상위 89% ~ 96%  (누적 96%)
     *   9등급: 상위 96% ~ 100% (누적100%)
     *
     * @param percentile 석차백분율 (0.0 ~ 100.0), 값이 작을수록 상위
     * @return "1" ~ "9" 등급 문자열
     */
    public static String calculateRankByPercentile(double percentile) {
        if (percentile <= 4)  return "1";
        if (percentile <= 11) return "2";
        if (percentile <= 23) return "3";
        if (percentile <= 40) return "4";
        if (percentile <= 60) return "5";
        if (percentile <= 77) return "6";
        if (percentile <= 89) return "7";
        if (percentile <= 96) return "8";
        return "9";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
