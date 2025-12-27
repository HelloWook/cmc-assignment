package com.example.cmc.service;

import com.example.cmc.dto.request.CategoryCreateRequest;
import com.example.cmc.dto.response.CategoryResponse;
import com.example.cmc.entity.Category;
import com.example.cmc.exception.NotFoundException;
import com.example.cmc.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 단위 테스트")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("테스트 카테고리")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new CategoryCreateRequest();
        createRequest.setName("새 카테고리");
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_Success() {
        // given
        Category savedCategory = Category.builder()
                .id(1L)
                .name("새 카테고리")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // when
        CategoryResponse response = categoryService.createCategory(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("새 카테고리");

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("모든 카테고리 조회 성공")
    void getAllCategories_Success() {
        // given
        Category category2 = Category.builder()
                .id(2L)
                .name("두 번째 카테고리")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category, category2));

        // when
        List<CategoryResponse> responses = categoryService.getAllCategories();

        // then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);

        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("카테고리 ID로 조회 성공")
    void getCategoryById_Success() {
        // given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // when
        CategoryResponse response = categoryService.getCategoryById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("테스트 카테고리");

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("카테고리 ID로 조회 실패 - 존재하지 않는 카테고리")
    void getCategoryById_Fail_NotFound() {
        // given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("카테고리를 찾을 수 없습니다.");

        verify(categoryRepository).findById(999L);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() {
        // given
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        // when
        categoryService.deleteCategory(1L);

        // then
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 카테고리")
    void deleteCategory_Fail_NotFound() {
        // given
        when(categoryRepository.existsById(999L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("카테고리를 찾을 수 없습니다.");

        verify(categoryRepository).existsById(999L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}
