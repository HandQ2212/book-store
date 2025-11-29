package com.btl.bookstore.service;

/*
Interface service chung cho các chức năng tiện ích
Cung cấp các phương thức quản lý session và định dạng tiền tệ
*/
public interface CommonService {

    // Xóa message trong session sau khi hiển thị
    void removeSessionMessage();

    // Lấy ký hiệu tiền tệ từ config
    String currencySign();
}