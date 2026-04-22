package com.edumanager.student;

import com.edumanager.parent.ParentProfile;
import com.edumanager.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parent_student_mappings")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_profile_id")
    private ParentProfile parentProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_profile_id")
    private Student student;

    private String relationship;
}
