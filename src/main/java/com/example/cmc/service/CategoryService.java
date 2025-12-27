package com.example.cmc.service;

import com.example.cmc.dto.request.CategoryCreateRequest;
import com.example.cmc.dto.request.CategoryUpdateRequest;
import com.example.cmc.dto.response.CategoryResponse;
import com.example.cmc.entity.Category;
import com.example.cmc.exception.NotFoundException;
import com.example.cmc.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        return toResponse(savedCategory);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        
        category.setName(request.getName());
        Category updatedCategory = categoryRepository.save(category);
        return toResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("카테고리를 찾을 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
