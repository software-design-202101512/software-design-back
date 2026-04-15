package com.edumanager.teacher;

import com.edumanager.student.Student;
import com.edumanager.subject.Subject;
import com.edumanager.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_students",
        uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "student_id", "is_homeroom", "subject_id"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "is_homeroom", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isHomeroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = true)
    private Subject subject;
}
