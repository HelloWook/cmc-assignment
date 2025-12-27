package com.example.cmc.controller.view;

import com.example.cmc.dto.request.*;
import com.example.cmc.dto.response.*;
import com.example.cmc.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ViewController {

    private final AuthService authService;
    private final PostService postService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    @GetMapping
    public String home(@RequestParam(required = false) Long categoryId, Model model, HttpSession session) {
        List<PostResponse> posts;
        if (categoryId != null) {
            List<PostResponse> allPosts = postService.getAllPosts();
            posts = allPosts.stream()
                    .filter(post -> post.getCategories() != null && 
                            post.getCategories().stream()
                                    .anyMatch(cat -> cat.getId().equals(categoryId)))
                    .toList();
        } else {
            posts = postService.getAllPosts();
        }
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        addUserToModel(model, session);
        return "index";
    }

    @GetMapping("/signup")
    public String signupPage(Model model, HttpSession session) {
        if (authService.isLoggedIn(session)) {
            return "redirect:/";
        }
        model.addAttribute("signUpRequest", new SignUpRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignUpRequest request, RedirectAttributes redirectAttributes) {
        try {
            authService.signUp(request);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (authService.isLoggedIn(session)) {
            return "redirect:/";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            authService.login(request, session);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        authService.logout(session);
        return "redirect:/";
    }

    @GetMapping("/posts/{id}")
    public String postDetail(@PathVariable Long id, Model model, HttpSession session) {
        try {
            PostResponse post = postService.getPostById(id);
            List<CommentResponse> comments = commentService.getCommentsByPostId(id);
            List<CategoryResponse> categories = categoryService.getAllCategories();
            
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("categories", categories);
            model.addAttribute("commentRequest", new CommentCreateRequest());
            addUserToModel(model, session);
            return "post-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/posts/new")
    public String postForm(Model model, HttpSession session) {
        if (!authService.isLoggedIn(session)) {
            return "redirect:/login";
        }
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("postRequest", new PostCreateRequest());
        model.addAttribute("categories", categories);
        addUserToModel(model, session);
        return "post-form";
    }

    @PostMapping("/posts")
    public String createPost(@ModelAttribute PostCreateRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            UserResponse user = authService.getCurrentUser(session);
            request.setAuthorEmail(user.getEmail());
            PostResponse post = postService.createPost(request);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 작성되었습니다.");
            return "redirect:/posts/" + post.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 작성에 실패했습니다.");
            return "redirect:/posts/new";
        }
    }

    @GetMapping("/posts/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model, HttpSession session) {
        try {
            PostResponse post = postService.getPostById(id);
            UserResponse user = authService.getCurrentUser(session);
            
            if (!post.getAuthorEmail().equals(user.getEmail()) && !user.getRole().equals("ADMIN")) {
                return "redirect:/posts/" + id;
            }
            
            PostUpdateRequest updateRequest = new PostUpdateRequest();
            updateRequest.setTitle(post.getTitle());
            updateRequest.setContent(post.getContent());
            if (post.getCategories() != null) {
                updateRequest.setCategoryIds(post.getCategories().stream()
                    .map(CategoryInfo::getId)
                    .toList());
            }
            
            List<CategoryResponse> categories = categoryService.getAllCategories();
            model.addAttribute("post", post);
            model.addAttribute("postRequest", updateRequest);
            model.addAttribute("categories", categories);
            addUserToModel(model, session);
            return "post-edit";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/posts/{id}/edit")
    public String updatePost(@PathVariable Long id, @ModelAttribute PostUpdateRequest request, 
                             HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            PostResponse post = postService.getPostById(id);
            UserResponse user = authService.getCurrentUser(session);
            
            if (!post.getAuthorEmail().equals(user.getEmail()) && !user.getRole().equals("ADMIN")) {
                return "redirect:/posts/" + id;
            }
            
            postService.updatePost(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 수정되었습니다.");
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정에 실패했습니다.");
            return "redirect:/posts/" + id + "/edit";
        }
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            PostResponse post = postService.getPostById(id);
            UserResponse user = authService.getCurrentUser(session);
            
            if (!post.getAuthorEmail().equals(user.getEmail()) && !user.getRole().equals("ADMIN")) {
                return "redirect:/posts/" + id;
            }
            
            postService.deletePost(id);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 삭제에 실패했습니다.");
            return "redirect:/posts/" + id;
        }
    }

    @PostMapping("/posts/{postId}/comments")
    public String createComment(@PathVariable Long postId, 
                                @RequestParam(required = false) Long parentId,
                                @RequestParam String content,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            UserResponse user = authService.getCurrentUser(session);
            CommentCreateRequest request = new CommentCreateRequest();
            request.setAuthorEmail(user.getEmail());
            request.setPostId(postId);
            request.setContent(content);
            request.setParentId(parentId);
            commentService.createComment(request);
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 작성되었습니다.");
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 작성에 실패했습니다.");
            return "redirect:/posts/" + postId;
        }
    }

    @GetMapping("/comments/{id}/edit")
    public String editCommentForm(@PathVariable Long id, Model model, HttpSession session) {
        try {
            return "redirect:/";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/comments/{id}/edit")
    public String updateComment(@PathVariable Long id, @RequestParam String content,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            CommentResponse comment = commentService.getCommentById(id);
            UserResponse user = authService.getCurrentUser(session);
            
            if (!comment.getAuthorEmail().equals(user.getEmail()) && !user.getRole().equals("ADMIN")) {
                return "redirect:/posts/" + comment.getPostId();
            }
            
            CommentUpdateRequest request = new CommentUpdateRequest();
            request.setContent(content);
            commentService.updateComment(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 수정되었습니다.");
            return "redirect:/posts/" + comment.getPostId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 수정에 실패했습니다.");
            return "redirect:/";
        }
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            CommentResponse comment = commentService.getCommentById(id);
            UserResponse user = authService.getCurrentUser(session);
            
            if (!comment.getAuthorEmail().equals(user.getEmail()) && !user.getRole().equals("ADMIN")) {
                return "redirect:/posts/" + comment.getPostId();
            }
            
            Long postId = comment.getPostId();
            commentService.deleteComment(id);
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 삭제되었습니다.");
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 삭제에 실패했습니다.");
            return "redirect:/";
        }
    }

    @GetMapping("/categories")
    public String categoriesPage(Model model, HttpSession session) {
        if (!authService.isLoggedIn(session)) {
            return "redirect:/login";
        }
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("categoryRequest", new CategoryCreateRequest());
        addUserToModel(model, session);
        return "category-list";
    }

    @PostMapping("/categories")
    public String createCategory(@ModelAttribute CategoryCreateRequest request,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        if (!authService.isLoggedIn(session)) {
            return "redirect:/login";
        }
        try {
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("successMessage", "카테고리가 생성되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "카테고리 생성에 실패했습니다.");
        }
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id, @RequestParam String name,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        if (!authService.isLoggedIn(session)) {
            return "redirect:/login";
        }
        try {
            com.example.cmc.dto.request.CategoryUpdateRequest request = 
                new com.example.cmc.dto.request.CategoryUpdateRequest();
            request.setName(name);
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "카테고리가 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "카테고리 수정에 실패했습니다.");
        }
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!authService.isLoggedIn(session)) {
            return "redirect:/login";
        }
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "카테고리가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "카테고리 삭제에 실패했습니다.");
        }
        return "redirect:/categories";
    }

    private void addUserToModel(Model model, HttpSession session) {
        if (authService.isLoggedIn(session)) {
            try {
                UserResponse user = authService.getCurrentUser(session);
                model.addAttribute("currentUser", user);
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("isAdmin", authService.isAdmin(session));
            } catch (Exception e) {
                model.addAttribute("isLoggedIn", false);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}

