package com.btl.bookstore.model;


import java.util.Date;

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
Entity đại diện cho user
Chứa thông tin cá nhân, địa chỉ, xác thực và bảo mật
*/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class UserDtls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255)
    private String name; // Tên

    private String mobileNumber; // SĐT

    private String email; // Email

    @Column(length = 500)
    private String address; // Địa chỉ

    @Column(length = 255)
    private String city; // Thành phố

    @Column(length = 255)
    private String state; // Tỉnh

    private String pincode; // Mã bưu điện

    private String password; // Mật khẩu mã hóa

    private String profileImage; // Ảnh đại diện

    private String role; // Vai trò (USER/ADMIN)

    private Boolean isEnable;

    private Boolean accountNonLocked;

    private Integer failedAttempt;

    private Date lockTime;

    private String resetToken;

    private String verificationToken;

}

