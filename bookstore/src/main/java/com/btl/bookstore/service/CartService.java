package com.btl.bookstore.service;

import com.btl.bookstore.model.Cart;

import java.util.List;


public interface CartService {

    public Cart saveCart(Integer bookId, Integer userId);

    public List<Cart> getCartsByUser(Integer userId);

    public Integer getCountCart(Integer userId);

    public Cart updateQuantity(String sy, Integer cid);
    
    public Cart updateQuantityDirect(Integer cid, Integer quantity);

    public void updateCartSelection(List<Integer> selectedIds, Integer userId);

}