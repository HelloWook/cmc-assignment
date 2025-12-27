package com.example.cmc.service;

import com.example.cmc.dto.request.BookmarkCreateRequest;
import com.example.cmc.dto.response.BookmarkResponse;
import com.example.cmc.dto.response.CategoryInfo;
import com.example.cmc.dto.response.PostResponse;
import com.example.cmc.entity.Bookmark;
import com.example.cmc.entity.Category;
import com.example.cmc.entity.Post;
import com.example.cmc.entity.PostCategory;
import com.example.cmc.exception.BadRequestException;
import com.example.cmc.exception.NotFoundException;
import com.example.cmc.repository.BookmarkRepository;
import com.example.cmc.repository.CategoryRepository;
import com.example.cmc.repository.PostCategoryRepository;
import com.example.cmc.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BookmarkResponse createBookmark(BookmarkCreateRequest request) {
        if (bookmarkRepository.existsByUserEmailAndPostId(request.getUserEmail(), request.getPostId())) {
            throw new BadRequestException("이미 북마크된 게시글입니다.");
        }

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        Bookmark bookmark = Bookmark.builder()
                .userEmail(request.getUserEmail())
                .postId(request.getPostId())
                .build();
        
        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        PostResponse postResponse = toPostResponse(post);
        
        return BookmarkResponse.builder()
                .userEmail(savedBookmark.getUserEmail())
                .postId(savedBookmark.getPostId())
                .post(postResponse)
                .createdAt(savedBookmark.getCreatedAt())
                .build();
    }

    public List<BookmarkResponse> getBookmarksByUser(String userEmail) {
        // JOIN FETCH를 사용하여 N+1 문제 해결
        List<Bookmark> bookmarks = bookmarkRepository.findByUserEmailWithPost(userEmail);
        
        if (bookmarks.isEmpty()) {
            return List.of();
        }

        // Post ID 목록 수집
        List<Long> postIds = bookmarks.stream()
                .map(Bookmark::getPostId)
                .distinct()
                .collect(Collectors.toList());

        // 배치 조회: PostCategory를 한 번에 조회
        List<PostCategory> postCategories = postCategoryRepository.findByPostIdIn(postIds);
        
        // Category ID 목록 수집
        Set<Long> categoryIds = postCategories.stream()
                .map(PostCategory::getCategoryId)
                .collect(Collectors.toSet());

        // 배치 조회: Category를 한 번에 조회
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        // PostCategory를 Post ID별로 그룹화
        Map<Long, List<PostCategory>> postCategoryMap = postCategories.stream()
                .collect(Collectors.groupingBy(PostCategory::getPostId));

        // Post ID별 Post 엔티티 맵 생성
        Map<Long, Post> postMap = bookmarks.stream()
                .map(Bookmark::getPost)
                .filter(post -> post != null)
                .collect(Collectors.toMap(Post::getId, Function.identity(), (existing, replacement) -> existing));

        // BookmarkResponse 생성
        return bookmarks.stream()
                .map(bookmark -> {
                    Post post = postMap.get(bookmark.getPostId());
                    PostResponse postResponse = post != null 
                            ? toPostResponse(post, postCategoryMap.getOrDefault(bookmark.getPostId(), List.of()), categoryMap)
                            : null;
                    
                    return BookmarkResponse.builder()
                            .userEmail(bookmark.getUserEmail())
                            .postId(bookmark.getPostId())
                            .post(postResponse)
                            .createdAt(bookmark.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBookmark(String userEmail, Long postId) {
        if (!bookmarkRepository.existsByUserEmailAndPostId(userEmail, postId)) {
            throw new NotFoundException("북마크를 찾을 수 없습니다.");
        }
        bookmarkRepository.deleteById(new com.example.cmc.entity.BookmarkId(userEmail, postId));
    }

    private PostResponse toPostResponse(Post post) {
        List<PostCategory> postCategories = postCategoryRepository.findByPostId(post.getId());
        return toPostResponse(post, postCategories);
    }

    private PostResponse toPostResponse(Post post, List<PostCategory> postCategories) {
        Set<Long> categoryIds = postCategories.stream()
                .map(PostCategory::getCategoryId)
                .collect(Collectors.toSet());
        
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        return toPostResponse(post, postCategories, categoryMap);
    }

    private PostResponse toPostResponse(Post post, List<PostCategory> postCategories, Map<Long, Category> categoryMap) {
        List<CategoryInfo> categories = postCategories.stream()
                .map(pc -> {
                    Category category = categoryMap.get(pc.getCategoryId());
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

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorEmail(post.getAuthorEmail())
                .categories(categories)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
