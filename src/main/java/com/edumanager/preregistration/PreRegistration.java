package com.edumanager.preregistration;

import com.edumanager.user.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pre_registrations")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String name;

    private String email;

    // 학생 전용
    private String grade;
    private String classNum;
    private String studentNum;
    private String gender;

    // 교사 전용
    private Long subjectId;
    private Boolean isHomeroom;
    private String homeroomGrade;
    private String homeroomClassNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreRegistrationStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markMatched() {
        this.status = PreRegistrationStatus.MATCHED;
    }
}
