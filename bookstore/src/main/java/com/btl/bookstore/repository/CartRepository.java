package com.btl.bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.btl.bookstore.model.Cart;
import com.btl.bookstore.model.UserDtls;

import jakarta.transaction.Transactional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    public Cart findByBookIdAndUserId(Integer bookId, Integer userId);

    public Integer countByUserId(Integer userId);

    public List<Cart> findByUserId(Integer userId);

    @Transactional
    @Modifying
    public void deleteByUser(UserDtls user);

}
