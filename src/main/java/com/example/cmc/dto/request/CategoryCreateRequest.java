package com.example.cmc.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    @NotBlank(message = "카테고리 이름은 필수입니다")
    private String name;
}
