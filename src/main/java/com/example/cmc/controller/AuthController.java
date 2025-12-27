package com.example.cmc.controller;

import com.example.cmc.dto.request.LoginRequest;
import com.example.cmc.dto.request.SignUpRequest;
import com.example.cmc.dto.response.LoginResponse;
import com.example.cmc.dto.response.SignUpResponse;
import com.example.cmc.dto.response.UserResponse;
import com.example.cmc.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = authService.login(request, session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpSession session) {
        UserResponse user = authService.getCurrentUser(session);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> checkLoginStatus(HttpSession session) {
        boolean isLoggedIn = authService.isLoggedIn(session);
        return ResponseEntity.ok(isLoggedIn);
    }
}
