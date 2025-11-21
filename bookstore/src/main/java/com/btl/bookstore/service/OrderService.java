package com.btl.bookstore.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.btl.bookstore.model.OrderRequest;
import com.btl.bookstore.model.BookOrder;

public interface OrderService {
    public void saveOrder(Integer userId, OrderRequest orderRequest) throws Exception;

    public List<BookOrder> getOrdersByUser(Integer userId);

    public BookOrder updateOrderStatus(Integer id, String status);

    public List<BookOrder> getAllOrders();

    public BookOrder getOrdersByOrderId(String orderId);

    public Page<BookOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize);
}