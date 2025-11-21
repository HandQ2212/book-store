package com.btl.bookstore.repository;

import com.btl.bookstore.model.BookOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookOrderRepository extends JpaRepository<BookOrder, Integer> {
    List<BookOrder> findByUserId(Integer userId);

    BookOrder findByOrderId(String orderId);
}
