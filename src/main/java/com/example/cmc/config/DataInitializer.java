package com.example.cmc.config;

import com.example.cmc.entity.User;
import com.example.cmc.repository.UserRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRespository userRespository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NICKNAME = "관리자";

    @Override
    public void run(String... args) throws Exception {
        if (!userRespository.existsById(ADMIN_EMAIL)) {
            String encodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
            
            User admin = User.builder()
                    .email(ADMIN_EMAIL)
                    .password(encodedPassword)
                    .nickname(ADMIN_NICKNAME)
                    .role("ADMIN")
                    .build();

            userRespository.save(admin);
            log.info("ADMIN 계정이 생성되었습니다. 이메일: {}, 비밀번호: {}", ADMIN_EMAIL, ADMIN_PASSWORD);
        } else {
            log.info("ADMIN 계정이 이미 존재합니다.");
        }
    }
}
