package com.btl.bookstore.util;


import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.btl.bookstore.model.BookOrder;
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

    public static String generateUrl(HttpServletRequest request) {

        String siteUrl = request.getRequestURL().toString();

        return siteUrl.replace(request.getServletPath(), "");
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