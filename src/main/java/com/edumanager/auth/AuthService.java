package com.edumanager.auth;

import com.edumanager.exception.EmailAlreadyExistsException;
import com.edumanager.exception.InvalidCredentialsException;
import com.edumanager.user.User;
import com.edumanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(request.role())
                .subject(request.subject())
                .grade(request.grade())
                .classNum(request.classNum())
                .studentNum(request.studentNum())
                .childName(request.childName())
                .build();

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(buildAuthentication(user.getEmail()));
        return new AuthResponse(token, new AuthResponse.UserDto(
                String.valueOf(user.getId()), user.getName(), user.getEmail(), user.getRole()));
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

        String token = jwtTokenProvider.generateToken(buildAuthentication(user.getEmail()));
        return new AuthResponse(token, new AuthResponse.UserDto(
                String.valueOf(user.getId()), user.getName(), user.getEmail(), user.getRole()));
    }

    private Authentication buildAuthentication(String email) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
