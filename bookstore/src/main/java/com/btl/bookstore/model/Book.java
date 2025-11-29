package com.btl.bookstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
Entity đại diện cho sách
Chứa thông tin về tên, giá, mô tả, tồn kho và giảm giá
*/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 500)
    private String title; // Tên sách

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả

    @Column(length = 255)
    private String category; // Danh mục

    private Double price; // Giá gốc

    private int stock; // Số lượng tồn

    private String image; // Link ảnh

    private int discount; // % giảm giá

    private Double discountPrice; // Giá sau giảm

    private Boolean isActive; // Trạng thái

}
