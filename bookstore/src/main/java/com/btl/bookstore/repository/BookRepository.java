package com.btl.bookstore.repository;

import com.btl.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByIsActiveTrue();

    Page<Book> findByIsActiveTrue(Pageable pageable);

    List<Book> findByCategory(String category);

    Page<Book> findByIsActiveTrueAndCategory(String category, Pageable pageable);

    List<Book> findByTitleContainingIgnoreCase(String ch);

    Page<Book> findByTitleContainingIgnoreCase(String ch, Pageable pageable);

    Page<Book> findByIsActiveTrueAndTitleContainingIgnoreCase(String ch, Pageable pageable);

    Page<Book> findByIsActiveTrueAndCategoryAndTitleContainingIgnoreCase(String category, String ch, Pageable pageable);
}