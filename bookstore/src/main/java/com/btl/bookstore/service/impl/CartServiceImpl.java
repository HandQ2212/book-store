package com.btl.bookstore.service.impl;

import com.btl.bookstore.model.Cart;
import com.btl.bookstore.model.Book;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.repository.BookRepository;
import com.btl.bookstore.repository.CartRepository;
import com.btl.bookstore.repository.UserRepository;
import com.btl.bookstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public Cart saveCart(Integer bookId, Integer userId) {

        UserDtls userDtls = userRepository.findById(userId).get();
        Book book = bookRepository.findById(bookId).get();

        Cart cartStatus = cartRepository.findByBookIdAndUserId(bookId, userId);

        Cart cart = null;

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setBook(book);
            cart.setUser(userDtls);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * book.getDiscountPrice());
        } else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * cart.getBook().getDiscountPrice());
        }
        Cart saveCart = cartRepository.save(cart);

        return saveCart;
    }

    @Override
    public List<Cart> getCartsByUser(Integer userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

        Double totalOrderPrice = 0.0;
        List<Cart> updateCarts = new ArrayList<>();
        for (Cart cart : carts) {
            Double totalPrice = (cart.getBook().getDiscountPrice() * cart.getQuantity());
            cart.setTotalPrice(totalPrice);
            totalOrderPrice = totalOrderPrice + totalPrice;
            cart.setTotalOrderPrice(totalOrderPrice);
            updateCarts.add(cart);
        }

        return updateCarts;
    }

    @Override
    public Integer getCountCart(Integer userId) {
        Integer countByUserId = cartRepository.countByUserId(userId);
        return countByUserId;
    }

    @Override
    public void updateQuantity(String sy, Integer cid) {

        Cart cart = cartRepository.findById(cid).get();
        int updateQuantity;

        if (sy.equalsIgnoreCase("de")) {
            updateQuantity = cart.getQuantity() - 1;

            if (updateQuantity <= 0) {
                cartRepository.delete(cart);
            } else {
                cart.setQuantity(updateQuantity);
                cartRepository.save(cart);
            }
        } else {
            updateQuantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart);
        }

    }
}
