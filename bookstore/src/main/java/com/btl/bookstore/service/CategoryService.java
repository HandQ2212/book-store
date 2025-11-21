package com.btl.bookstore.service;

import java.util.List;

import com.btl.bookstore.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    public Category saveCategory(Category category);

    public Boolean existCategory(String name);

    public List<Category> getAllCategory();

    public Boolean deleteCategory(int id);

    public Category getCategoryById(int id);

    public List<Category> getAllActiveCategory();

    public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize);
}
