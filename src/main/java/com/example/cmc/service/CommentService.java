package com.example.cmc.service;

import com.example.cmc.dto.request.CommentCreateRequest;
import com.example.cmc.dto.request.CommentUpdateRequest;
import com.example.cmc.dto.response.CommentResponse;
import com.example.cmc.entity.Comment;
import com.example.cmc.entity.User;
import com.example.cmc.repository.CommentRepository;
import com.example.cmc.repository.UserRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRespository userRespository;

    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        Comment comment = Comment.builder()
                .content(request.getContent())
                .authorEmail(request.getAuthorEmail())
                .postId(request.getPostId())
                .parentId(request.getParentId())
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        return toResponse(savedComment);
    }

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        return toResponse(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long id, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return toResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new NotFoundException("댓글을 찾을 수 없습니다.");
        }
        commentRepository.deleteById(id);
    }

    private CommentResponse toResponse(Comment comment) {
        String authorNickname = null;
        if (comment.getAuthorEmail() != null) {
            User user = userRespository.findById(comment.getAuthorEmail()).orElse(null);
            if (user != null) {
                authorNickname = user.getNickname();
            }
        }
        
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorEmail(comment.getAuthorEmail())
                .authorNickname(authorNickname)
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}