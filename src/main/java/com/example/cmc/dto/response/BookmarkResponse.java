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
public class BookmarkResponse {
    private String userEmail;
    private Long postId;
    private PostResponse post;
    private LocalDateTime createdAt;
}
