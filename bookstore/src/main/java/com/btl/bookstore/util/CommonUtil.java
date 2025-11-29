package com.btl.bookstore.util;


import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.btl.bookstore.model.BookOrder;
import com.btl.bookstore.model.OrderAddress;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    @Value("${spring.mail.username}")
    private String mailUsername;

    public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(mailUsername, "Book Store");
        helper.setTo(reciepentEmail);

        String content = "<p>Hello,</p>" 
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>" 
                + "<p><a href=\"" + url + "\" style=\"background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;\">Reset Password</a></p>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "<p>This link will expire in 24 hours.</p>";
        helper.setSubject("Password Reset Request");
        helper.setText(content, true);
        mailSender.send(message);
        return true;
    }

    public Boolean sendVerificationMail(String url, String recipientEmail, String userName) throws UnsupportedEncodingException, MessagingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(mailUsername, "Book Store");
        helper.setTo(recipientEmail);

        String content = "<p>Hello <b>" + userName + "</b>,</p>" 
                + "<p>Thank you for registering with Book Store!</p>"
                + "<p>Please click the link below to verify your email address:</p>" 
                + "<p><a href=\"" + url + "\" style=\"background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;\">Verify Email</a></p>"
                + "<p>If you did not create an account, please ignore this email.</p>"
                + "<p>This link will expire in 24 hours.</p>";
        helper.setSubject("Email Verification - Book Store");
        helper.setText(content, true);
        mailSender.send(message);
        return true;
    }

    public static String generateUrl(HttpServletRequest request) {

        String siteUrl = request.getRequestURL().toString();

        return siteUrl.replace(request.getServletPath(), "");
    }

    public static String formatPrice(Number price) {
        if (price == null) {
            return "0";
        }
        return String.format("%,d", price.longValue()).replace(',', '.');
    }

    String msg=null;;

    public Boolean sendMailForBookOrder(BookOrder order,String status) throws Exception
    {

        msg="<p>Hello <b>[[name]]</b>,</p>"
                + "<p>Thank you for your order! Your order status is: <b style=\"color: #28a745;\">[[orderStatus]]</b></p>"
                + "<p><b>Order Details:</b></p>"
                + "<table style=\"border-collapse: collapse; width: 100%; margin: 20px 0;\">"
                + "<tr style=\"background-color: #f5f5f5;\">"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\"><b>Book Title</b></td>"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\">[[bookName]]</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\"><b>Category</b></td>"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\">[[category]]</td>"
                + "</tr>"
                + "<tr style=\"background-color: #f5f5f5;\">"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\"><b>Quantity</b></td>"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\">[[quantity]]</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\"><b>Price</b></td>"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\">[[price]]</td>"
                + "</tr>"
                + "<tr style=\"background-color: #f5f5f5;\">"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\"><b>Payment Type</b></td>"
                + "<td style=\"padding: 10px; border: 1px solid #ddd;\">[[paymentType]]</td>"
                + "</tr>"
                + "</table>"
                + "<p>Thank you for shopping with us!</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(mailUsername, "Book Store");
        helper.setTo(order.getOrderAddress().getEmail());

        msg=msg.replace("[[name]]",order.getOrderAddress().getFirstName());
        msg=msg.replace("[[orderStatus]]",status);
        msg=msg.replace("[[bookName]]", order.getBook().getTitle());
        msg=msg.replace("[[category]]", order.getBook().getCategory());
        msg=msg.replace("[[quantity]]", order.getQuantity().toString());
        msg=msg.replace("[[price]]", order.getPrice().toString());
        msg=msg.replace("[[paymentType]]", order.getPaymentType());

        helper.setSubject("Book Order Status Update");
        helper.setText(msg, true);
        mailSender.send(message);
        return true;
    }

    public Boolean sendMailForMultipleOrders(List<BookOrder> orders, OrderAddress address, String paymentType, String status) throws Exception {
        if (orders == null || orders.isEmpty()) {
            return false;
        }

        // Calculate totals
        Double subtotal = 0.0;
        Double shippingFee = 2500.0; // Phí vận chuyển
        
        // Build books table rows
        StringBuilder booksRows = new StringBuilder();
        for (BookOrder order : orders) {
            Double itemTotal = order.getPrice() * order.getQuantity();
            subtotal += itemTotal;
            
            booksRows.append("<tr>")
                    .append("<td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">").append(order.getBook().getTitle()).append("</td>")
                    .append("<td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">").append(order.getBook().getCategory()).append("</td>")
                    .append("<td style=\"padding: 8px; border-bottom: 1px solid #ddd; text-align: center;\">").append(order.getQuantity()).append("</td>")
                    .append("<td style=\"padding: 8px; border-bottom: 1px solid #ddd; text-align: right;\">").append(String.format("%,.0f", order.getPrice())).append(" đồng</td>")
                    .append("<td style=\"padding: 8px; border-bottom: 1px solid #ddd; text-align: right;\">").append(String.format("%,.0f", itemTotal)).append(" đồng</td>")
                    .append("</tr>");
        }
        
        Double taxAmount = subtotal * 0.05; // Thuế 5%
        Double totalAmount = subtotal + shippingFee + taxAmount;
        
        // Build full name
        String fullName = address.getFirstName();
        if (address.getLastName() != null && !address.getLastName().trim().isEmpty()) {
            fullName += " " + address.getLastName();
        }

        String emailMsg = "<div style=\"font-family: Arial, sans-serif; max-width: 650px; margin: 0 auto; padding: 20px; background-color: #ffffff;\">"
                + "<h2 style=\"color: #333; margin-bottom: 20px;\">Order Confirmation</h2>"
                + "<p>Hello <b>" + fullName + "</b>,</p>"
                + "<p>Thank you for your order. Order status: <b>" + status.toUpperCase() + "</b></p>"
                
                + "<h3 style=\"color: #333; margin-top: 25px; margin-bottom: 15px;\">Order Details</h3>"
                + "<table style=\"border-collapse: collapse; width: 100%; margin-bottom: 20px;\">"
                + "<thead>"
                + "<tr style=\"background-color: #f5f5f5;\">"
                + "<th style=\"padding: 10px 8px; border-bottom: 2px solid #ddd; text-align: left;\">Book Title</th>"
                + "<th style=\"padding: 10px 8px; border-bottom: 2px solid #ddd; text-align: left;\">Category</th>"
                + "<th style=\"padding: 10px 8px; border-bottom: 2px solid #ddd; text-align: center;\">Quantity</th>"
                + "<th style=\"padding: 10px 8px; border-bottom: 2px solid #ddd; text-align: right;\">Unit Price</th>"
                + "<th style=\"padding: 10px 8px; border-bottom: 2px solid #ddd; text-align: right;\">Total</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>"
                + booksRows.toString()
                + "</tbody>"
                + "</table>"
                
                + "<h3 style=\"color: #333; margin-top: 25px; margin-bottom: 15px;\">Payment Summary</h3>"
                + "<table style=\"width: 100%; margin-bottom: 20px;\">"
                + "<tr>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Subtotal</td>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee; text-align: right;\">" + String.format("%,.0f", subtotal) + " đồng</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Shipping Fee</td>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee; text-align: right;\">" + String.format("%,.0f", shippingFee) + " đồng</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Tax (5%)</td>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee; text-align: right;\">" + String.format("%,.0f", taxAmount) + " đồng</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding: 12px 0 8px 0; border-top: 2px solid #333; font-size: 16px;\"><b>TOTAL AMOUNT</b></td>"
                + "<td style=\"padding: 12px 0 8px 0; border-top: 2px solid #333; text-align: right; font-size: 18px;\"><b>" + String.format("%,.0f", totalAmount) + " đồng</b></td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Payment Method</td>"
                + "<td style=\"padding: 8px 0; border-bottom: 1px solid #eee; text-align: right;\">" + paymentType + "</td>"
                + "</tr>"
                + "</table>"
                
                + "<h3 style=\"color: #333; margin-top: 25px; margin-bottom: 15px;\">Shipping Address</h3>"
                + "<div style=\"background-color: #f9f9f9; padding: 15px; margin-bottom: 20px;\">"
                + "<p style=\"margin: 5px 0;\"><b>Name:</b> " + fullName + "</p>"
                + "<p style=\"margin: 5px 0;\"><b>Email:</b> " + address.getEmail() + "</p>"
                + "<p style=\"margin: 5px 0;\"><b>Phone:</b> " + address.getMobileNo() + "</p>"
                + "<p style=\"margin: 5px 0;\"><b>Address:</b> " + address.getAddress() + ", " + address.getCity() + ", " + address.getState() + " - " + address.getPincode() + "</p>"
                + "</div>"
                
                + "<p style=\"color: #666; font-size: 14px; margin-top: 30px;\">Thank you for shopping with Book Store!</p>"
                + "<p style=\"color: #999; font-size: 12px;\">If you have any questions, please contact our customer support.</p>"
                + "</div>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(mailUsername, "Book Store");
        helper.setTo(address.getEmail());
        helper.setSubject("Order Confirmation - Book Store");
        helper.setText(emailMsg, true);
        
        mailSender.send(message);
        return true;
    }

    public UserDtls getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        UserDtls userDtls = userService.getUserByEmail(email);
        return userDtls;
    }

    public String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "/img/default.jpg";
        }
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        return "/img/" + imagePath;
    }
}