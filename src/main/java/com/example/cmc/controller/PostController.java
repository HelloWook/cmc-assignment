package com.example.cmc.controller;

import com.example.cmc.dto.request.PostCreateRequest;
import com.example.cmc.dto.request.PostUpdateRequest;
import com.example.cmc.dto.response.PostResponse;
import com.example.cmc.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/author/{authorEmail}")
    public ResponseEntity<List<PostResponse>> getPostsByAuthor(@PathVariable String authorEmail) {
        List<PostResponse> posts = postService.getPostsByAuthorEmail(authorEmail);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id, 
            @Valid @RequestBody PostUpdateRequest request) {
        PostResponse response = postService.updatePost(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
