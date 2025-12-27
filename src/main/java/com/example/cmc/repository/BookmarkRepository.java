package com.example.cmc.repository;

import com.example.cmc.entity.Bookmark;
import com.example.cmc.entity.BookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {
    List<Bookmark> findByUserEmail(String userEmail);
    
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.post WHERE b.userEmail = :userEmail")
    List<Bookmark> findByUserEmailWithPost(@Param("userEmail") String userEmail);
    
    boolean existsByUserEmailAndPostId(String userEmail, Long postId);
}
