package com.btl.bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.btl.bookstore.model.UserDtls;

public interface UserRepository extends JpaRepository<UserDtls, Integer> {

    @Query("SELECT u FROM UserDtls u WHERE LOWER(u.email) = LOWER(?1)")
    public UserDtls findByEmail(String email);

    public List<UserDtls> findByRole(String role);

    public UserDtls findByResetToken(String token);

    public UserDtls findByVerificationToken(String token);

    public Boolean existsByEmail(String email);
}

