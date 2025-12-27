package com.example.cmc.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;
    private int status;

    public static ErrorResponse of(String message, int status) {
        return ErrorResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }
}

