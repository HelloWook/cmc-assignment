package com.example.cmc.service;

import com.example.cmc.dto.request.CommentCreateRequest;
import com.example.cmc.dto.request.CommentUpdateRequest;
import com.example.cmc.entity.Comment;
import com.example.cmc.exception.NotFoundException;
import com.example.cmc.repository.CommentRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private CommentCreateRequest createRequest;
    private CommentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testComment = Comment.builder()
                .id(1L)
                .content("테스트 댓글")
                .authorEmail("test@example.com")
                .postId(1L)
                .parentId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new CommentCreateRequest();
        createRequest.setContent("테스트 댓글");
        createRequest.setAuthorEmail("test@example.com");
        createRequest.setPostId(1L);
        createRequest.setParentId(null);

        updateRequest = new CommentUpdateRequest();
        updateRequest.setContent("수정된 댓글");
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_Success() {
        // given
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // when
        var response = commentService.createComment(createRequest);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("테스트 댓글", response.getContent());
        assertEquals("test@example.com", response.getAuthorEmail());
        assertEquals(1L, response.getPostId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 생성 성공 - 대댓글")
    void createComment_Success_Reply() {
        // given
        createRequest.setParentId(1L);
        testComment.setParentId(1L);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // when
        var response = commentService.createComment(createRequest);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getParentId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("게시글 ID로 댓글 조회 성공")
    void getCommentsByPostId_Success() {
        // given
        Comment comment2 = Comment.builder()
                .id(2L)
                .content("댓글2")
                .authorEmail("test2@example.com")
                .postId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(commentRepository.findByPostId(1L)).thenReturn(Arrays.asList(testComment, comment2));

        // when
        List<?> responses = commentService.getCommentsByPostId(1L);

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(commentRepository, times(1)).findByPostId(1L);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // when
        var response = commentService.updateComment(1L, updateRequest);

        // then
        assertNotNull(response);
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않음")
    void updateComment_Fail_NotFound() {
        // given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> commentService.updateComment(1L, updateRequest));
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        when(commentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(commentRepository).deleteById(1L);

        // when
        commentService.deleteComment(1L);

        // then
        verify(commentRepository, times(1)).existsById(1L);
        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않음")
    void deleteComment_Fail_NotFound() {
        // given
        when(commentRepository.existsById(1L)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> commentService.deleteComment(1L));
        verify(commentRepository, times(1)).existsById(1L);
        verify(commentRepository, never()).deleteById(anyLong());
    }
}
