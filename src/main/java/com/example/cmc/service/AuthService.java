package com.example.cmc.service;

import com.example.cmc.dto.request.LoginRequest;
import com.example.cmc.dto.request.SignUpRequest;
import com.example.cmc.dto.response.LoginResponse;
import com.example.cmc.dto.response.SignUpResponse;
import com.example.cmc.dto.response.UserResponse;
import com.example.cmc.entity.User;
import com.example.cmc.exception.BadRequestException;
import com.example.cmc.exception.UnauthorizedException;
import com.example.cmc.repository.UserRespository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRespository userRespository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final String SESSION_USER_KEY = "user";
    private static final String DEFAULT_ROLE = "USER";

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRespository.existsById(request.getEmail())) {
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(DEFAULT_ROLE)
                .build();

        userRespository.save(user);

        return SignUpResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpSession session) {
        User user = userRespository.findById(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        UserResponse userResponse = UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
        
        session.setAttribute(SESSION_USER_KEY, userResponse);

        return LoginResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .message("로그인에 성공했습니다.")
                .build();
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public UserResponse getCurrentUser(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return user;
    }

    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_USER_KEY) != null;
    }

    public boolean isAdmin(HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute(SESSION_USER_KEY);
        return user != null && "ADMIN".equals(user.getRole());
    }
}
