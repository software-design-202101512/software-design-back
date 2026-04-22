package com.edumanager.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRegistrationRequest {
    private String email;
    private String name;
    private Long subjectId;
    private Boolean isHomeroom;
    private String homeroomGrade;
    private String homeroomClassNum;
}
