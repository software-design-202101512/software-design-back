package com.edumanager.auth;

import com.edumanager.exception.EmailAlreadyExistsException;
import com.edumanager.exception.InvalidCredentialsException;
import com.edumanager.parent.ParentProfile;
import com.edumanager.parent.ParentProfileRepository;
import com.edumanager.preregistration.PreRegistration;
import com.edumanager.preregistration.PreRegistrationRepository;
import com.edumanager.preregistration.PreRegistrationStatus;
import com.edumanager.student.ParentStudent;
import com.edumanager.student.ParentStudentRepository;
import com.edumanager.student.Student;
import com.edumanager.student.StudentRepository;
import com.edumanager.subject.Subject;
import com.edumanager.subject.SubjectRepository;
import com.edumanager.teacher.TeacherProfile;
import com.edumanager.teacher.TeacherProfileRepository;
import com.edumanager.teacher.TeacherStudentMappingService;
import com.edumanager.user.Role;
import com.edumanager.user.User;
import com.edumanager.user.UserRepository;
import com.edumanager.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SubjectRepository subjectRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final PreRegistrationRepository preRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TeacherStudentMappingService teacherStudentMappingService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // ADMIN 역할로 가입 차단
        if (request.role() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 일반 가입으로 생성할 수 없습니다.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // 교사/학생은 사전등록 데이터와 매칭 확인
        PreRegistration preReg = null;
        if (request.role() == Role.TEACHER) {
            preReg = preRegistrationRepository.findByRoleAndNameAndEmailAndStatus(
                    Role.TEACHER, request.name(), request.email(), PreRegistrationStatus.WAITING)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "사전등록 정보와 일치하지 않습니다. 관리자에게 문의하세요."));
        } else if (request.role() == Role.STUDENT) {
            preReg = preRegistrationRepository.findByRoleAndNameAndGradeAndClassNumAndStudentNumAndStatus(
                    Role.STUDENT, request.name(), request.grade(), request.classNum(),
                    request.studentNum(), PreRegistrationStatus.WAITING)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "사전등록 정보와 일치하지 않습니다. 관리자에게 문의하세요."));
        }

        // 학부모 검증
        Student studentForParent = null;
        if (request.role() == Role.PARENT) {
            if ((request.invitationCode() == null || request.invitationCode().isBlank()) && (request.childName() == null || request.childName().isBlank())) {
                throw new IllegalArgumentException("학부모 가입을 위해서는 초대 코드 또는 자녀 이름이 필수입니다.");
            }
            if (request.invitationCode() != null && !request.invitationCode().isBlank()) {
                studentForParent = studentRepository.findByInvitationCode(request.invitationCode())
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));
            } else {
                studentForParent = studentRepository.findAll().stream()
                        .filter(s -> s.getUser().getName().equals(request.childName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("해당 이름의 학생을 찾을 수 없습니다."));
            }
        }

        // 사전등록 매칭된 교사/학생은 바로 APPROVED, 학부모는 PENDING
        UserStatus initialStatus = (preReg != null) ? UserStatus.APPROVED : UserStatus.PENDING;

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(request.role())
                .status(initialStatus)
                .build();

        userRepository.save(user);

        // 역할에 따른 프로필 생성
        if (request.role() == Role.STUDENT) {
            String invitationCode = String.format("%s-%s-%s-INV",
                    request.grade(), request.classNum(), request.studentNum());
            studentRepository.save(Student.builder()
                    .user(user)
                    .grade(request.grade())
                    .classNum(request.classNum())
                    .studentNum(request.studentNum())
                    .gender(preReg != null ? preReg.getGender() : request.gender())
                    .invitationCode(invitationCode)
                    .build());
        } else if (request.role() == Role.TEACHER) {
            Subject subject = null;
            if (preReg != null && preReg.getSubjectId() != null) {
                subject = subjectRepository.findById(preReg.getSubjectId()).orElse(null);
            } else if (request.subject() != null) {
                subject = subjectRepository.findByName(request.subject())
                        .orElseGet(() -> subjectRepository.save(Subject.builder().name(request.subject()).build()));
            }
            teacherProfileRepository.save(TeacherProfile.builder()
                    .user(user)
                    .subject(subject)
                    .isHomeroom(preReg != null ? preReg.getIsHomeroom() : false)
                    .homeroomGrade(preReg != null ? preReg.getHomeroomGrade() : null)
                    .homeroomClassNum(preReg != null ? preReg.getHomeroomClassNum() : null)
                    .build());
        } else if (request.role() == Role.PARENT && studentForParent != null) {
            ParentProfile parentProfile = parentProfileRepository.save(ParentProfile.builder()
                    .user(user)
                    .phoneNumber(request.phoneNumber())
                    .build());

            parentStudentRepository.save(ParentStudent.builder()
                    .parentProfile(parentProfile)
                    .student(studentForParent)
                    .relationship(request.relationship() != null ? request.relationship() : "학부모")
                    .build());
        }

        // 사전등록 레코드를 MATCHED로 업데이트
        if (preReg != null) {
            preReg.markMatched();
        }

        if (initialStatus == UserStatus.APPROVED) {
            // 사전등록 매칭된 사용자 → 교사-학생 자동 매핑
            if (user.getRole() == Role.TEACHER) {
                teacherStudentMappingService.mapTeacherToExistingStudents(user);
            } else if (user.getRole() == Role.STUDENT) {
                teacherStudentMappingService.mapStudentToExistingTeachers(user);
            }

            String token = jwtTokenProvider.generateToken(buildAuthentication(user.getEmail()));
            return new AuthResponse(token, new AuthResponse.UserDto(
                    String.valueOf(user.getId()), user.getName(), user.getEmail(), user.getRole()),
                    "사전등록 정보와 일치합니다. 회원가입이 완료되었습니다.");
        }

        return new AuthResponse(null, new AuthResponse.UserDto(
                String.valueOf(user.getId()), user.getName(), user.getEmail(), user.getRole()),
                "회원가입이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다.");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!user.getRole().equals(request.role())) {
            throw new InvalidCredentialsException("역할이 일치하지 않습니다.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new InvalidCredentialsException("가입 승인 대기 중입니다. 관리자 승인 후 로그인할 수 있습니다.");
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new InvalidCredentialsException("가입이 거절되었습니다. 관리자에게 문의하세요.");
        }

        String token = jwtTokenProvider.generateToken(buildAuthentication(user.getEmail()));
        return new AuthResponse(token, new AuthResponse.UserDto(
                String.valueOf(user.getId()), user.getName(), user.getEmail(), user.getRole()));
    }

    private Authentication buildAuthentication(String email) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
