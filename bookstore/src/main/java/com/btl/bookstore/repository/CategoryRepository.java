package com.btl.bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.btl.bookstore.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    public Boolean existsByName(String name);

    public Boolean existsByNameIgnoreCase(String name);

    public List<Category> findByIsActiveTrue();

}