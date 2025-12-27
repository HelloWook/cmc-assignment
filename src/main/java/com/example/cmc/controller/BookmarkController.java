package com.example.cmc.controller;

import com.example.cmc.dto.request.BookmarkCreateRequest;
import com.example.cmc.dto.response.BookmarkResponse;
import com.example.cmc.service.BookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    
    private final BookmarkService bookmarkService;

    @PostMapping("/create")
    public ResponseEntity<BookmarkResponse> createBookmark(@Valid @RequestBody BookmarkCreateRequest request) {
        BookmarkResponse response = bookmarkService.createBookmark(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<BookmarkResponse>> getBookmarksByUser(@PathVariable String userEmail) {
        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarksByUser(userEmail);
        return ResponseEntity.ok(bookmarks);
    }

    @DeleteMapping("/user/{userEmail}/post/{postId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable String userEmail,
            @PathVariable Long postId) {
        bookmarkService.deleteBookmark(userEmail, postId);
        return ResponseEntity.noContent().build();
    }
}
