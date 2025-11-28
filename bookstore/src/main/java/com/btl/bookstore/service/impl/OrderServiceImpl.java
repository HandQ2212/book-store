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
import com.btl.bookstore.model.Book;
import com.btl.bookstore.repository.CartRepository;
import com.btl.bookstore.repository.BookOrderRepository;
import com.btl.bookstore.repository.BookRepository;
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
    private BookRepository bookRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {

        List<Cart> carts = cartRepository.findByUserId(userid);
        List<BookOrder> ordersList = new java.util.ArrayList<>();
        OrderAddress sharedAddress = null;
        String paymentType = orderRequest.getPaymentType();

        for (Cart cart : carts) {
            
            // Chỉ tạo đơn hàng cho những items được chọn
            if (cart.getSelected() == null || !cart.getSelected()) {
                continue;
            }

            BookOrder order = new BookOrder();

            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());

            order.setBook(cart.getBook());
            order.setPrice(cart.getBook().getDiscountPrice());

            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());

            order.setStatus(OrderStatus.IN_PROGRESS.getName());
            order.setPaymentType(paymentType);

            OrderAddress address = new OrderAddress();
            address.setFirstName(orderRequest.getFirstName());
            address.setLastName(orderRequest.getLastName());
            address.setEmail(orderRequest.getEmail());
            address.setMobileNo(orderRequest.getMobileNo());
            address.setAddress(orderRequest.getAddress());
            address.setCity(orderRequest.getCity());
            address.setState(orderRequest.getState());
            address.setPincode(orderRequest.getPincode());

            order.setOrderAddress(address);
            
            if (sharedAddress == null) {
                sharedAddress = address;
            }

            BookOrder saveOrder = orderRepository.save(order);
            ordersList.add(saveOrder);
            
            // Reduce stock after successful order
            Book book = cart.getBook();
            int newStock = book.getStock() - cart.getQuantity();
            book.setStock(newStock);
            bookRepository.save(book);
        }
        
        // Gửi 1 mail duy nhất với tất cả đơn hàng
        if (!ordersList.isEmpty()) {
            commonUtil.sendMailForMultipleOrders(ordersList, sharedAddress, paymentType, "success");
        }
        
        // Xóa cart sau khi đặt hàng (chỉ xóa items đã được chọn)
        resetSelectedCart(userid);
    }
    
    private void resetSelectedCart(Integer userid) {
        List<Cart> carts = cartRepository.findByUserId(userid);
        for (Cart cart : carts) {
            if (cart.getSelected() != null && cart.getSelected()) {
                cartRepository.delete(cart);
            }
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
    
    @Override
    public Page<BookOrder> getOrdersByStatusPagination(String status, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return orderRepository.findByStatus(status, pageable);
    }

}


