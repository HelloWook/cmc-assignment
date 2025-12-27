package com.example.cmc.service;

import com.example.cmc.dto.request.LoginRequest;
import com.example.cmc.dto.request.SignUpRequest;
import com.example.cmc.dto.response.SignUpResponse;
import com.example.cmc.entity.User;
import com.example.cmc.exception.BadRequestException;
import com.example.cmc.exception.UnauthorizedException;
import com.example.cmc.repository.UserRespository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserRespository userRespository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트유저")
                .role("USER")
                .build();

        signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setNickname("테스트유저");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        when(userRespository.existsById(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRespository.save(any(User.class))).thenReturn(testUser);

        // when
        SignUpResponse response = authService.signUp(signUpRequest);

        // then
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("테스트유저", response.getNickname());
        assertEquals("회원가입이 완료되었습니다.", response.getMessage());
        verify(userRespository, times(1)).existsById(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRespository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_Fail_DuplicateEmail() {
        // given
        when(userRespository.existsById(anyString())).thenReturn(true);

        // when & then
        assertThrows(BadRequestException.class, () -> authService.signUp(signUpRequest));
        verify(userRespository, times(1)).existsById(anyString());
        verify(userRespository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        when(userRespository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        doNothing().when(httpSession).setAttribute(anyString(), any());

        // when
        var response = authService.login(loginRequest, httpSession);

        // then
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("테스트유저", response.getNickname());
        assertEquals("로그인에 성공했습니다.", response.getMessage());
        verify(userRespository, times(1)).findById(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(httpSession, times(1)).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_Fail_UserNotFound() {
        // given
        when(userRespository.findById(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, httpSession));
        verify(userRespository, times(1)).findById(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        // given
        when(userRespository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when & then
        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, httpSession));
        verify(userRespository, times(1)).findById(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        doNothing().when(httpSession).invalidate();

        // when
        authService.logout(httpSession);

        // then
        verify(httpSession, times(1)).invalidate();
    }

    @Test
    @DisplayName("현재 사용자 조회 성공")
    void getCurrentUser_Success() {
        // given
        var userResponse = com.example.cmc.dto.response.UserResponse.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .role("USER")
                .build();
        when(httpSession.getAttribute(anyString())).thenReturn(userResponse);

        // when
        var result = authService.getCurrentUser(httpSession);

        // then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("테스트유저", result.getNickname());
    }

    @Test
    @DisplayName("현재 사용자 조회 실패 - 로그인 안됨")
    void getCurrentUser_Fail_NotLoggedIn() {
        // given
        when(httpSession.getAttribute(anyString())).thenReturn(null);

        // when & then
        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUser(httpSession));
    }

    @Test
    @DisplayName("로그인 상태 확인 - 로그인됨")
    void isLoggedIn_True() {
        // given
        when(httpSession.getAttribute(anyString())).thenReturn(new Object());

        // when
        boolean result = authService.isLoggedIn(httpSession);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("로그인 상태 확인 - 로그인 안됨")
    void isLoggedIn_False() {
        // given
        when(httpSession.getAttribute(anyString())).thenReturn(null);

        // when
        boolean result = authService.isLoggedIn(httpSession);

        // then
        assertFalse(result);
    }
}
