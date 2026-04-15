package com.edumanager.record;

import com.edumanager.student.Student;
import com.edumanager.subject.Subject;
import com.edumanager.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_records")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = true)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    private RecordCategory category;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_visible_to_student", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isVisibleToStudent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateVisibility(boolean visible) {
        this.isVisibleToStudent = visible;
        this.updatedAt = LocalDateTime.now();
    }
}
