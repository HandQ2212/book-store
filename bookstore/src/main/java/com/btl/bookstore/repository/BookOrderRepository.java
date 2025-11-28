package com.btl.bookstore.repository;

import com.btl.bookstore.model.BookOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookOrderRepository extends JpaRepository<BookOrder, Integer> {
    List<BookOrder> findByUserId(Integer userId);

    BookOrder findByOrderId(String orderId);
    
    boolean existsByBookId(Integer bookId);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM BookOrder o WHERE o.book.id = :bookId AND o.status NOT IN ('Delivered', 'Cancelled')")
    boolean existsByBookIdAndNotDelivered(@Param("bookId") Integer bookId);
    
    Page<BookOrder> findByStatus(String status, Pageable pageable);
}
