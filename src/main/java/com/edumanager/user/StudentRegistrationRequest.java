package com.edumanager.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRegistrationRequest {
    private String email;
    private String name;
    private String grade;
    private String classNum;
    private String studentNum;
    private String gender;
}
