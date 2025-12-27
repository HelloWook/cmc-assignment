package com.example.cmc.service;

import com.example.cmc.dto.request.PostCreateRequest;
import com.example.cmc.dto.request.PostUpdateRequest;
import com.example.cmc.entity.Category;
import com.example.cmc.entity.Post;
import com.example.cmc.entity.PostCategory;
import com.example.cmc.exception.NotFoundException;
import com.example.cmc.repository.CategoryRepository;
import com.example.cmc.repository.PostCategoryRepository;
import com.example.cmc.repository.PostRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostCategoryRepository postCategoryRepository;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testPost = Post.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .authorEmail("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new PostCreateRequest();
        createRequest.setTitle("테스트 제목");
        createRequest.setContent("테스트 내용");
        createRequest.setAuthorEmail("test@example.com");
        createRequest.setCategoryIds(Arrays.asList(1L, 2L));

        updateRequest = new PostUpdateRequest();
        updateRequest.setTitle("수정된 제목");
        updateRequest.setContent("수정된 내용");
        updateRequest.setCategoryIds(Arrays.asList(3L));
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_Success() {
        // given
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(postCategoryRepository.saveAll(anyList())).thenReturn(List.of());

        // when
        var response = postService.createPost(createRequest);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("테스트 제목", response.getTitle());
        assertEquals("테스트 내용", response.getContent());
        verify(postRepository, times(1)).save(any(Post.class));
        verify(postCategoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("게시글 생성 성공 - 카테고리 없음")
    void createPost_Success_NoCategories() {
        // given
        createRequest.setCategoryIds(null);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // when
        var response = postService.createPost(createRequest);

        // then
        assertNotNull(response);
        verify(postRepository, times(1)).save(any(Post.class));
        verify(postCategoryRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공")
    void getPostById_Success() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postCategoryRepository.findByPostId(1L)).thenReturn(List.of());

        // when
        var response = postService.getPostById(1L);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("테스트 제목", response.getTitle());
        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시글 ID로 조회 실패 - 존재하지 않음")
    void getPostById_Fail_NotFound() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> postService.getPostById(1L));
        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("모든 게시글 조회 성공")
    void getAllPosts_Success() {
        // given
        Post post2 = Post.builder()
                .id(2L)
                .title("제목2")
                .content("내용2")
                .authorEmail("test2@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(postRepository.findAll()).thenReturn(Arrays.asList(testPost, post2));
        when(postCategoryRepository.findByPostId(anyLong())).thenReturn(List.of());

        // when
        List<?> responses = postService.getAllPosts();

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(postRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("작성자 이메일로 게시글 조회 성공")
    void getPostsByAuthorEmail_Success() {
        // given
        when(postRepository.findByAuthorEmail("test@example.com")).thenReturn(Arrays.asList(testPost));
        when(postCategoryRepository.findByPostId(anyLong())).thenReturn(List.of());

        // when
        List<?> responses = postService.getPostsByAuthorEmail("test@example.com");

        // then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(postRepository, times(1)).findByAuthorEmail("test@example.com");
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        doNothing().when(postCategoryRepository).deleteByPostId(1L);
        when(postCategoryRepository.saveAll(anyList())).thenReturn(List.of());
        when(postCategoryRepository.findByPostId(1L)).thenReturn(List.of());

        // when
        var response = postService.updatePost(1L, updateRequest);

        // then
        assertNotNull(response);
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
        verify(postCategoryRepository, times(1)).deleteByPostId(1L);
        verify(postCategoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않음")
    void updatePost_Fail_NotFound() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> postService.updatePost(1L, updateRequest));
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() {
        // given
        when(postRepository.existsById(1L)).thenReturn(true);
        doNothing().when(postRepository).deleteById(1L);

        // when
        postService.deletePost(1L);

        // then
        verify(postRepository, times(1)).existsById(1L);
        verify(postRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않음")
    void deletePost_Fail_NotFound() {
        // given
        when(postRepository.existsById(1L)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> postService.deletePost(1L));
        verify(postRepository, times(1)).existsById(1L);
        verify(postRepository, never()).deleteById(anyLong());
    }
}
