package com.edumanager;

import com.edumanager.grade.*;
import com.edumanager.record.*;
import com.edumanager.student.*;
import com.edumanager.subject.*;
import com.edumanager.teacher.*;
import com.edumanager.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherStudentRepository teacherStudentRepository;
    private final GradeRepository gradeRepository;
    private final StudentRecordRepository recordRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        String pw = passwordEncoder.encode("password123");

        // 교사 3명
        User teacher1 = userRepository.save(User.builder().email("teacher1@test.com").password(pw).name("김담임").role(Role.TEACHER).build());
        User teacher2 = userRepository.save(User.builder().email("teacher2@test.com").password(pw).name("이국어").role(Role.TEACHER).build());
        User teacher3 = userRepository.save(User.builder().email("teacher3@test.com").password(pw).name("박수학").role(Role.TEACHER).build());

        // 학생 User 3명 + Student 엔티티 3개
        User su1 = userRepository.save(User.builder().email("student1@test.com").password(pw).name("홍길동").role(Role.STUDENT).build());
        Student st1 = studentRepository.save(Student.builder().user(su1).grade("2").classNum("3").studentNum("1").build());

        User su2 = userRepository.save(User.builder().email("student2@test.com").password(pw).name("김철수").role(Role.STUDENT).build());
        Student st2 = studentRepository.save(Student.builder().user(su2).grade("2").classNum("3").studentNum("2").build());

        User su3 = userRepository.save(User.builder().email("student3@test.com").password(pw).name("이영희").role(Role.STUDENT).build());
        Student st3 = studentRepository.save(Student.builder().user(su3).grade("2").classNum("3").studentNum("3").build());

        // 학부모 1명
        User parent1 = userRepository.save(User.builder().email("parent1@test.com").password(pw).name("홍부모").role(Role.PARENT).build());
        parentStudentRepository.save(ParentStudent.builder().parent(parent1).student(st1).relationship("부모").build());

        // 과목 3개
        Subject korean = subjectRepository.save(Subject.builder().name("국어").category("국어교과").build());
        Subject math = subjectRepository.save(Subject.builder().name("수학").category("수학교과").build());
        Subject english = subjectRepository.save(Subject.builder().name("영어").category("영어교과").build());

        // 교사-학생 연결
        // 김담임: 담임으로 홍길동, 김철수, 이영희
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher1).student(st1).isHomeroom(true).subject(null).build());
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher1).student(st2).isHomeroom(true).subject(null).build());
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher1).student(st3).isHomeroom(true).subject(null).build());

        // 이국어: 교과(국어)로 홍길동, 김철수, 이영희
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher2).student(st1).isHomeroom(false).subject(korean).build());
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher2).student(st2).isHomeroom(false).subject(korean).build());
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher2).student(st3).isHomeroom(false).subject(korean).build());

        // 박수학: 교과(수학)로 홍길동, 김철수
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher3).student(st1).isHomeroom(false).subject(math).build());
        teacherStudentRepository.save(TeacherStudent.builder().teacher(teacher3).student(st2).isHomeroom(false).subject(math).build());

        // 성적 데이터 (2025년 1학기)
        // 홍길동
        gradeRepository.save(Grade.builder().student(st1).subject(korean).teacher(teacher2).year(2025).semester(1).score(new BigDecimal("85")).rank("B").build());
        gradeRepository.save(Grade.builder().student(st1).subject(math).teacher(teacher3).year(2025).semester(1).score(new BigDecimal("92")).rank("A").build());
        gradeRepository.save(Grade.builder().student(st1).subject(english).teacher(teacher1).year(2025).semester(1).score(new BigDecimal("78")).rank("C").build());
        // 김철수
        gradeRepository.save(Grade.builder().student(st2).subject(korean).teacher(teacher2).year(2025).semester(1).score(new BigDecimal("70")).rank("C").build());
        gradeRepository.save(Grade.builder().student(st2).subject(math).teacher(teacher3).year(2025).semester(1).score(new BigDecimal("88")).rank("B").build());
        // 이영희
        gradeRepository.save(Grade.builder().student(st3).subject(korean).teacher(teacher2).year(2025).semester(1).score(new BigDecimal("95")).rank("A").build());
        gradeRepository.save(Grade.builder().student(st3).subject(english).teacher(teacher1).year(2025).semester(1).score(new BigDecimal("90")).rank("A").build());

        // 학생부 데이터
        // 홍길동
        recordRepository.save(StudentRecord.builder().student(st1).teacher(teacher1).subject(null).category(RecordCategory.BEHAVIOR).content("수업 태도가 성실하고 협동심이 뛰어남").isVisibleToStudent(true).build());
        recordRepository.save(StudentRecord.builder().student(st1).teacher(teacher1).subject(null).category(RecordCategory.SPECIAL_NOTE).content("학급 반장으로 리더십 발휘").isVisibleToStudent(true).build());
        recordRepository.save(StudentRecord.builder().student(st1).teacher(teacher2).subject(korean).category(RecordCategory.SUBJECT_EVALUATION).content("국어 발표력이 우수하고 글쓰기 능력이 뛰어남").isVisibleToStudent(false).build());
        // 김철수
        recordRepository.save(StudentRecord.builder().student(st2).teacher(teacher1).subject(null).category(RecordCategory.BEHAVIOR).content("성실하고 책임감이 강함").isVisibleToStudent(true).build());
        recordRepository.save(StudentRecord.builder().student(st2).teacher(teacher3).subject(math).category(RecordCategory.SUBJECT_EVALUATION).content("수학적 사고력이 뛰어나며 문제해결 능력이 우수").isVisibleToStudent(true).build());
        // 이영희
        recordRepository.save(StudentRecord.builder().student(st3).teacher(teacher1).subject(null).category(RecordCategory.BEHAVIOR).content("모범적인 학교생활 태도").isVisibleToStudent(true).build());
        recordRepository.save(StudentRecord.builder().student(st3).teacher(teacher2).subject(korean).category(RecordCategory.SUBJECT_EVALUATION).content("독서량이 많고 언어 감각이 탁월함").isVisibleToStudent(true).build());
        recordRepository.save(StudentRecord.builder().student(st3).teacher(teacher1).subject(null).category(RecordCategory.ATTENDANCE).content("무결석 개근 예정").isVisibleToStudent(true).build());
    }
}
