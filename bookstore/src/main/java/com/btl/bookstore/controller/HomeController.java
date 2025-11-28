package com.btl.bookstore.controller;

import com.btl.bookstore.model.Book;
import com.btl.bookstore.model.Category;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.service.BookService;
import com.btl.bookstore.service.CartService;
import com.btl.bookstore.service.CategoryService;
import com.btl.bookstore.service.CloudinaryService;
import com.btl.bookstore.service.UserService;
import com.btl.bookstore.util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @ModelAttribute
    public void getUserDetails(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            m.addAttribute("user", userDtls);
            Integer countCart = cartService.getCountCart(userDtls.getId());
            m.addAttribute("countCart", countCart);
        }

        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categorys", allActiveCategory);
    }

    @GetMapping("/")
    public String index(Model m) {

        List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
                .sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();
        List<Book> allActiveBooks = bookService.getAllActiveBooks("").stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();
        m.addAttribute("category", allActiveCategory);
        m.addAttribute("books", allActiveBooks);
        return "index";
    }

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/books")
    public String books(Model m, @RequestParam(value = "category", defaultValue = "") String category,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
                           @RequestParam(defaultValue = "") String ch) {

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramValue", category);
        m.addAttribute("categories", categories);

        Page<Book> page = null;
        if (StringUtils.isEmpty(ch)) {
            page = bookService.getAllActiveBookPagination(pageNo, pageSize, category);
        } else {
            page = bookService.searchActiveBookPagination(pageNo, pageSize, category, ch);
        }

        List<Book> books = page.getContent();
        m.addAttribute("books", books);
        m.addAttribute("booksSize", books.size());

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "book";
    }

    @GetMapping("/book/{id}")
    public String book(@PathVariable int id, Model m, Principal principal) {
        Book bookById = bookService.getBookById(id);
        m.addAttribute("book", bookById);
        return "view_book";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
            throws IOException {

        Boolean existsEmail = userService.existsEmail(user.getEmail());

        if (existsEmail) {
            session.setAttribute("errorMsg", "Email already exist");
        } else {
            try {
                String imageName = "default.jpg";
                
                if (file != null && !file.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(file, "profile");
                    imageName = imageUrl;
                }
                
                user.setProfileImage(imageName);
                UserDtls saveUser = userService.saveUser(user);

                if (!ObjectUtils.isEmpty(saveUser)) {
                    session.setAttribute("succMsg", "Register successfully");
                } else {
                    session.setAttribute("errorMsg", "something wrong on server");
                }
            } catch (Exception e) {
                session.setAttribute("errorMsg", "Error uploading image: " + e.getMessage());
            }
        }

        return "redirect:/register";
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot_password.html";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
            throws UnsupportedEncodingException, MessagingException {

        logger.info("Forgot password request for email: {}", email);
        String trimmedEmail = email.trim();
        logger.info("Trimmed email: {}", trimmedEmail);
        
        UserDtls userByEmail = userService.getUserByEmail(trimmedEmail);
        logger.info("User found: {}", userByEmail != null ? "Yes" : "No");

        if (ObjectUtils.isEmpty(userByEmail)) {
            logger.warn("User not found for email: {}", trimmedEmail);
            session.setAttribute("errorMsg", "Invalid email");
        } else {
            logger.info("Generating reset token for user: {}", userByEmail.getEmail());
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(trimmedEmail, resetToken);

            String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;
            logger.info("Reset URL: {}", url);

            Boolean sendMail = commonUtil.sendMail(url, trimmedEmail);
            logger.info("Email sent: {}", sendMail);

            if (sendMail) {
                session.setAttribute("succMsg", "Please check your email..Password Reset link sent");
            } else {
                logger.error("Failed to send email");
                session.setAttribute("errorMsg", "Somethong wrong on server ! Email not send");
            }
        }

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {

        UserDtls userByToken = userService.getUserByToken(token);

        if (userByToken == null) {
            m.addAttribute("msg", "Your link is invalid or expired !!");
            return "message";
        }
        m.addAttribute("token", token);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
                                Model m) {

        UserDtls userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            m.addAttribute("errorMsg", "Your link is invalid or expired !!");
            return "message";
        } else {
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);
            m.addAttribute("msg", "Password change successfully");

            return "message";
        }

    }

    @GetMapping("/search")
    public String searchBook(@RequestParam String ch, Model m) {
        List<Book> searchBooks = bookService.searchBook(ch);
        m.addAttribute("books", searchBooks);
        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("categories", categories);
        return "book";
    }

    @GetMapping("/api/search-suggestions")
    @ResponseBody
    public List<Book> getSearchSuggestions(@RequestParam String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        List<Book> books = bookService.searchBook(query.trim());
        return books.stream().limit(10).toList();
    }

}
