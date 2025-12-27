package com.example.cmc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String authorEmail;
    private String authorNickname;
    private List<CategoryInfo> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
