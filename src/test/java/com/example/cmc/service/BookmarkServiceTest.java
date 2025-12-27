package com.example.cmc.service;

import com.example.cmc.dto.request.BookmarkCreateRequest;
import com.example.cmc.entity.Bookmark;
import com.example.cmc.entity.BookmarkId;
import com.example.cmc.entity.Post;
import com.example.cmc.exception.BadRequestException;
import com.example.cmc.exception.NotFoundException;
import com.example.cmc.repository.BookmarkRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkService 단위 테스트")
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCategoryRepository postCategoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    private Post testPost;
    private Bookmark testBookmark;
    private BookmarkCreateRequest createRequest;

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

        testBookmark = Bookmark.builder()
                .userEmail("user@example.com")
                .postId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new BookmarkCreateRequest();
        createRequest.setUserEmail("user@example.com");
        createRequest.setPostId(1L);
    }

    @Test
    @DisplayName("북마크 생성 성공")
    void createBookmark_Success() {
        // given
        when(bookmarkRepository.existsByUserEmailAndPostId(anyString(), anyLong())).thenReturn(false);
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);
        when(postCategoryRepository.findByPostId(1L)).thenReturn(List.of());

        // when
        var response = bookmarkService.createBookmark(createRequest);

        // then
        assertNotNull(response);
        assertEquals("user@example.com", response.getUserEmail());
        assertEquals(1L, response.getPostId());
        assertNotNull(response.getPost());
        verify(bookmarkRepository, times(1)).existsByUserEmailAndPostId(anyString(), anyLong());
        verify(postRepository, times(1)).findById(1L);
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 생성 실패 - 이미 존재함")
    void createBookmark_Fail_AlreadyExists() {
        // given
        when(bookmarkRepository.existsByUserEmailAndPostId(anyString(), anyLong())).thenReturn(true);

        // when & then
        assertThrows(BadRequestException.class, () -> bookmarkService.createBookmark(createRequest));
        verify(bookmarkRepository, times(1)).existsByUserEmailAndPostId(anyString(), anyLong());
        verify(postRepository, never()).findById(anyLong());
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 생성 실패 - 게시글 없음")
    void createBookmark_Fail_PostNotFound() {
        // given
        when(bookmarkRepository.existsByUserEmailAndPostId(anyString(), anyLong())).thenReturn(false);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> bookmarkService.createBookmark(createRequest));
        verify(bookmarkRepository, times(1)).existsByUserEmailAndPostId(anyString(), anyLong());
        verify(postRepository, times(1)).findById(1L);
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("사용자 북마크 목록 조회 성공")
    void getBookmarksByUser_Success() {
        // given
        when(bookmarkRepository.findByUserEmailWithPost("user@example.com")).thenReturn(List.of(testBookmark));
        when(postCategoryRepository.findByPostIdIn(anyList())).thenReturn(List.of());

        // when
        List<?> responses = bookmarkService.getBookmarksByUser("user@example.com");

        // then
        assertNotNull(responses);
        verify(bookmarkRepository, times(1)).findByUserEmailWithPost("user@example.com");
    }

    @Test
    @DisplayName("사용자 북마크 목록 조회 성공 - 빈 목록")
    void getBookmarksByUser_Success_Empty() {
        // given
        when(bookmarkRepository.findByUserEmailWithPost("user@example.com")).thenReturn(List.of());

        // when
        List<?> responses = bookmarkService.getBookmarksByUser("user@example.com");

        // then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(bookmarkRepository, times(1)).findByUserEmailWithPost("user@example.com");
    }

    @Test
    @DisplayName("북마크 삭제 성공")
    void deleteBookmark_Success() {
        // given
        when(bookmarkRepository.existsByUserEmailAndPostId("user@example.com", 1L)).thenReturn(true);
        doNothing().when(bookmarkRepository).deleteById(any(BookmarkId.class));

        // when
        bookmarkService.deleteBookmark("user@example.com", 1L);

        // then
        verify(bookmarkRepository, times(1)).existsByUserEmailAndPostId("user@example.com", 1L);
        verify(bookmarkRepository, times(1)).deleteById(any(BookmarkId.class));
    }

    @Test
    @DisplayName("북마크 삭제 실패 - 존재하지 않음")
    void deleteBookmark_Fail_NotFound() {
        // given
        when(bookmarkRepository.existsByUserEmailAndPostId("user@example.com", 1L)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> bookmarkService.deleteBookmark("user@example.com", 1L));
        verify(bookmarkRepository, times(1)).existsByUserEmailAndPostId("user@example.com", 1L);
        verify(bookmarkRepository, never()).deleteById(any(BookmarkId.class));
    }
}
