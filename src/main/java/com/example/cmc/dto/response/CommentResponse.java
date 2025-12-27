package com.example.cmc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String authorEmail;
    private String authorNickname;
    private Long postId;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
