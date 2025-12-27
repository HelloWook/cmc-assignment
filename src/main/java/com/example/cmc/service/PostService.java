package com.example.cmc.service;

import com.example.cmc.dto.request.PostCreateRequest;
import com.example.cmc.dto.request.PostUpdateRequest;
import com.example.cmc.dto.response.CategoryInfo;
import com.example.cmc.dto.response.PostResponse;
import com.example.cmc.entity.Category;
import com.example.cmc.entity.Post;
import com.example.cmc.entity.PostCategory;
import com.example.cmc.entity.User;
import com.example.cmc.repository.CategoryRepository;
import com.example.cmc.repository.PostCategoryRepository;
import com.example.cmc.repository.PostRepository;
import com.example.cmc.repository.UserRespository;
import com.example.cmc.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final UserRespository userRespository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorEmail(request.getAuthorEmail())
                .build();
        
        Post savedPost = postRepository.save(post);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<PostCategory> postCategories = request.getCategoryIds().stream()
                    .map(categoryId -> PostCategory.builder()
                            .postId(savedPost.getId())
                            .categoryId(categoryId)
                            .build())
                    .collect(Collectors.toList());
            postCategoryRepository.saveAll(postCategories);
        }

        return toResponse(savedPost);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
        return toResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getPostsByAuthorEmail(String authorEmail) {
        List<Post> posts = postRepository.findByAuthorEmail(authorEmail);
        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        Post updatedPost = postRepository.save(post);

        postCategoryRepository.deleteByPostId(id);
        
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<PostCategory> postCategories = request.getCategoryIds().stream()
                    .map(categoryId -> PostCategory.builder()
                            .postId(id)
                            .categoryId(categoryId)
                            .build())
                    .collect(Collectors.toList());
            postCategoryRepository.saveAll(postCategories);
        }

        return toResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("게시글을 찾을 수 없습니다.");
        }
        postRepository.deleteById(id);
    }

    private PostResponse toResponse(Post post) {
        List<PostCategory> postCategories = postCategoryRepository.findByPostId(post.getId());
        List<CategoryInfo> categories = postCategories.stream()
                .map(pc -> {
                    Category category = categoryRepository.findById(pc.getCategoryId())
                            .orElse(null);
                    if (category != null) {
                        return CategoryInfo.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .build();
                    }
                    return null;
                })
                .filter(category -> category != null)
                .collect(Collectors.toList());

        String authorNickname = null;
        if (post.getAuthorEmail() != null) {
            User user = userRespository.findById(post.getAuthorEmail()).orElse(null);
            if (user != null) {
                authorNickname = user.getNickname();
            }
        }
        
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorEmail(post.getAuthorEmail())
                .authorNickname(authorNickname)
                .categories(categories)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
