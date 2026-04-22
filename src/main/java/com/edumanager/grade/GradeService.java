package com.edumanager.grade;

import com.edumanager.exception.UnauthorizedException;
import com.edumanager.student.Student;
import com.edumanager.student.StudentRepository;
import com.edumanager.student.ParentStudentRepository;
import com.edumanager.subject.Subject;
import com.edumanager.subject.SubjectRepository;
import com.edumanager.teacher.TeacherProfile;
import com.edumanager.teacher.TeacherProfileRepository;
import com.edumanager.teacher.TeacherStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherStudentRepository teacherStudentRepository;
    private final ParentStudentRepository parentStudentRepository;

    /**
     * 교사가 성적을 입력합니다.
     * - teacher_student_mappings 테이블로 담당 학생 여부를 확인
     * - 저장 후 같은 과목·학년·학기 전체 학생의 9등급을 상대평가로 재산출
     */
    public GradeResponse createGrade(Long teacherUserId, GradeRequest request) {
        TeacherProfile teacherProfile = teacherProfileRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> new RuntimeException("교사 프로필을 찾을 수 없습니다."));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new RuntimeException("과목을 찾을 수 없습니다."));

        // 권한 체크: teacher_student_mappings에 관계가 있어야 성적 입력 가능
        if (!teacherStudentRepository.existsByTeacherProfileUserIdAndStudentId(teacherUserId, request.studentId())) {
            throw new UnauthorizedException("해당 학생의 성적을 입력할 권한이 없습니다.");
        }

        Grade grade = Grade.builder()
                .student(student)
                .subject(subject)
                .teacherProfile(teacherProfile)
                .year(request.year())
                .semester(request.semester())
                .score(request.score())
                .rank("-")  // 임시값, 아래에서 상대평가로 재산출
                .build();

        gradeRepository.save(grade);

        // 같은 과목·학년·학기 전체 학생의 등급을 상대평가로 재산출
        recalculateRanks(request.subjectId(), request.year(), request.semester());

        return toResponse(grade);
    }

    /**
     * 학생 성적 목록 조회 (역할별 접근 제어)
     * - TEACHER: 담당 학생만 조회 가능
     * - STUDENT: 본인 성적만 조회 가능 (Read Only)
     * - PARENT: 자녀 성적만 조회 가능 (Read Only)
     *
     * JOIN FETCH 쿼리로 N+1 문제를 방지합니다.
     */
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByStudent(Long studentId, Long requesterId, String requesterRole) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        switch (requesterRole) {
            case "TEACHER" -> {
                if (!teacherStudentRepository.existsByTeacherProfileUserIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 성적을 조회할 권한이 없습니다.");
                }
            }
            case "STUDENT" -> {
                if (!student.getUser().getId().equals(requesterId)) {
                    throw new UnauthorizedException("본인의 성적만 조회할 수 있습니다.");
                }
            }
            case "PARENT" -> {
                if (!parentStudentRepository.existsByParentProfileUserIdAndStudentId(requesterId, studentId)) {
                    throw new UnauthorizedException("해당 학생의 성적을 조회할 권한이 없습니다.");
                }
            }
            default -> throw new UnauthorizedException("권한이 없습니다.");
        }

        // JOIN FETCH 쿼리로 연관 엔티티를 한 번에 로딩 → N+1 방지
        return gradeRepository.findByStudentIdWithDetails(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 학기별 성적 요약 (총점, 평균, 종합등급)
     * - 과목별 점수를 합산하여 평균을 구함
     * - 과목별 등급의 평균(등급 숫자)으로 종합등급 산출
     */
    @Transactional(readOnly = true)
    public StudentGradeSummary getStudentGradeSummary(Long studentId, int year, int semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        // JOIN FETCH로 N+1 방지
        List<Grade> grades = gradeRepository.findByStudentIdAndYearAndSemesterWithDetails(studentId, year, semester);
        List<GradeResponse> gradeResponses = grades.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        BigDecimal totalScore = grades.stream()
                .map(Grade::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageScore = grades.isEmpty()
                ? BigDecimal.ZERO
                : totalScore.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);

        // 종합등급: 각 과목 등급(숫자)의 평균을 반올림
        String overallRank = "-";
        if (!grades.isEmpty()) {
            double avgRank = grades.stream()
                    .mapToInt(g -> {
                        try { return Integer.parseInt(g.getRank()); }
                        catch (NumberFormatException e) { return 5; }  // 파싱 실패 시 5등급으로 간주
                    })
                    .average()
                    .orElse(5.0);
            overallRank = String.valueOf(Math.round(avgRank));
        }

        return new StudentGradeSummary(
                student.getId(),
                student.getUser().getName(),
                year,
                semester,
                gradeResponses,
                averageScore,
                totalScore,
                overallRank
        );
    }

    /**
     * 성적 수정 (본인이 입력한 성적만 수정 가능)
     * - 점수 변경 후 해당 과목·학년·학기 전체 등급을 상대평가로 재산출
     */
    public GradeResponse updateGrade(Long gradeId, Long teacherUserId, GradeRequest request) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("성적을 찾을 수 없습니다."));

        if (!grade.getTeacherProfile().getUser().getId().equals(teacherUserId)) {
            throw new UnauthorizedException("본인이 입력한 성적만 수정할 수 있습니다.");
        }

        grade.updateScore(request.score(), grade.getRank());  // 점수만 먼저 업데이트
        gradeRepository.save(grade);

        // 같은 과목·학년·학기 전체 등급 재산출
        recalculateRanks(grade.getSubject().getId(), grade.getYear(), grade.getSemester());

        return toResponse(grade);
    }

    /**
     * 성적 삭제 (본인이 입력한 성적만 삭제 가능)
     * - 삭제 후 해당 과목·학년·학기 전체 등급을 상대평가로 재산출
     */
    public void deleteGrade(Long gradeId, Long teacherUserId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("성적을 찾을 수 없습니다."));

        if (!grade.getTeacherProfile().getUser().getId().equals(teacherUserId)) {
            throw new UnauthorizedException("본인이 입력한 성적만 삭제할 수 있습니다.");
        }

        Long subjectId = grade.getSubject().getId();
        int year = grade.getYear();
        int semester = grade.getSemester();

        gradeRepository.delete(grade);
        recalculateRanks(subjectId, year, semester);
    }

    /**
     * 상대평가 9등급 재산출
     *
     * 같은 과목·학년·학기의 모든 성적을 점수 내림차순으로 정렬한 뒤,
     * 각 학생의 석차백분율을 계산하여 9등급을 배정합니다.
     *
     * 석차백분율 = (석차 / 전체 인원) × 100
     * 동점자 처리: 동점인 학생들은 같은 석차를 부여 (중간 석차)
     *
     * 예시) 45명 중 1등 → 1/45×100 = 2.2% → 1등급
     *       45명 중 5등 → 5/45×100 = 11.1% → 3등급
     */
    private void recalculateRanks(Long subjectId, int year, int semester) {
        List<Grade> allGrades = gradeRepository
                .findBySubjectIdAndYearAndSemesterOrderByScoreDesc(subjectId, year, semester);

        int totalStudents = allGrades.size();
        if (totalStudents == 0) return;

        // 동점자를 고려한 석차 부여
        int currentRankPosition = 1;  // 현재 석차 (1부터 시작)
        for (int i = 0; i < totalStudents; i++) {
            Grade current = allGrades.get(i);

            // 이전 학생과 점수가 다르면 석차를 현재 위치(i+1)로 갱신
            if (i > 0 && current.getScore().compareTo(allGrades.get(i - 1).getScore()) != 0) {
                currentRankPosition = i + 1;
            }

            // 석차백분율 = 석차 / 전체인원 × 100
            double percentile = (double) currentRankPosition / totalStudents * 100.0;

            String newRank = Grade.calculateRankByPercentile(percentile);
            current.updateRank(newRank);
        }

        gradeRepository.saveAll(allGrades);
    }

    private GradeResponse toResponse(Grade grade) {
        return new GradeResponse(
                grade.getId(),
                grade.getStudent().getId(),
                grade.getStudent().getUser().getName(),
                grade.getSubject().getId(),
                grade.getSubject().getName(),
                grade.getYear(),
                grade.getSemester(),
                grade.getScore(),
                grade.getRank(),
                grade.getTeacherProfile().getUser().getName(),
                grade.getCreatedAt()
        );
    }
}
