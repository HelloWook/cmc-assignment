package com.example.cmc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String content;
    
    @NotBlank(message = "작성자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String authorEmail;
    
    @NotNull(message = "게시글 ID는 필수입니다")
    private Long postId;
    private Long parentId;
}
