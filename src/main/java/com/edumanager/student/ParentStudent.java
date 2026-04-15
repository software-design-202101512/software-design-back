package com.edumanager.student;

import com.edumanager.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parent_students")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String relationship;
}
