package com.example.cmc.repository;

import com.example.cmc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRespository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    
    void deleteByEmail(String email);
}