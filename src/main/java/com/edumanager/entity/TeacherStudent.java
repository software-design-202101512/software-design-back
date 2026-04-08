package com.edumanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_students",
        uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "student_id", "isHomeroom", "subject_id"}))
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

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isHomeroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = true)
    private Subject subject;
}
