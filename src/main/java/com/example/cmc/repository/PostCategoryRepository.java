package com.example.cmc.repository;

import com.example.cmc.entity.PostCategory;
import com.example.cmc.entity.PostCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, PostCategoryId> {
    List<PostCategory> findByPostId(Long postId);
    List<PostCategory> findByPostIdIn(List<Long> postIds);
    void deleteByPostId(Long postId);
}
