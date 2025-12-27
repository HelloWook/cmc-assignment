package com.example.cmc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    @NotBlank(message = "작성자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String authorEmail;
    
    private List<Long> categoryIds;
}
