package com.btl.bookstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class OrderAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255)
    private String firstName;

    @Column(length = 255)
    private String lastName;

    private String email;

    private String mobileNo;

    @Column(length = 500)
    private String address;

    @Column(length = 255)
    private String city;

    @Column(length = 255)
    private String state;

    private String pincode;
}