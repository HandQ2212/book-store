package com.btl.bookstore.service.impl;


import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.btl.bookstore.model.Cart;
import com.btl.bookstore.model.OrderAddress;
import com.btl.bookstore.model.OrderRequest;
import com.btl.bookstore.model.BookOrder;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.repository.CartRepository;
import com.btl.bookstore.repository.BookOrderRepository;
import com.btl.bookstore.repository.UserRepository;
import com.btl.bookstore.service.OrderService;
import com.btl.bookstore.util.CommonUtil;
import com.btl.bookstore.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private BookOrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {

        UserDtls user = userRepository.findById(userid).orElse(null);
        List<Cart> carts = cartRepository.findByUserId(userid);

        for (Cart cart : carts) {

            BookOrder order = new BookOrder();

            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());

            order.setBook(cart.getBook());
            order.setPrice(cart.getBook().getDiscountPrice());

            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());

            order.setStatus(OrderStatus.IN_PROGRESS.getName());
            order.setPaymentType(orderRequest.getPaymentType());

            OrderAddress address = new OrderAddress();
            // Auto-fill from user info
            address.setFirstName(user.getName());
            address.setLastName("");
            address.setEmail(user.getEmail());
            address.setMobileNo(user.getMobileNumber());
            address.setAddress(user.getAddress() != null ? user.getAddress() : orderRequest.getAddress());
            address.setCity(user.getCity() != null ? user.getCity() : orderRequest.getCity());

            order.setOrderAddress(address);

            BookOrder saveOrder = orderRepository.save(order);
            resetCart(cart.getUser());
            commonUtil.sendMailForBookOrder(saveOrder, "success");
        }
    }
    private void resetCart(UserDtls user) {
        cartRepository.deleteByUser(user);
    }
    @Override
    public List<BookOrder> getOrdersByUser(Integer userId) {
        List<BookOrder> orders = orderRepository.findByUserId(userId);
        return orders;
    }

    @Override
    public BookOrder updateOrderStatus(Integer id, String status) {
        Optional<BookOrder> findById = orderRepository.findById(id);
        if (findById.isPresent()) {
            BookOrder bookOrder = findById.get();
            bookOrder.setStatus(status);
            BookOrder updateOrder = orderRepository.save(bookOrder);
            return updateOrder;
        }
        return null;
    }

    @Override
    public List<BookOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Page<BookOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return orderRepository.findAll(pageable);

    }

    @Override
    public BookOrder getOrdersByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

}

