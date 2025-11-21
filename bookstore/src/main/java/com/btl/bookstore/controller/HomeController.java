package com.btl.bookstore.controller;

import com.btl.bookstore.model.Book;
import com.btl.bookstore.model.Category;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.service.BookService;
import com.btl.bookstore.service.CartService;
import com.btl.bookstore.service.CategoryService;
import com.btl.bookstore.service.UserService;
import com.btl.bookstore.util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

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

//		List<Book> books = bookService.getAllActiveBooks(category);
//		m.addAttribute("books", books);
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
    public String book(@PathVariable int id, Model m) {
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
            String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
            user.setProfileImage(imageName);
            UserDtls saveUser = userService.saveUser(user);

            if (!ObjectUtils.isEmpty(saveUser)) {
                if (!file.isEmpty()) {
                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
                            + file.getOriginalFilename());

//					System.out.println(path);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }
                session.setAttribute("succMsg", "Register successfully");
            } else {
                session.setAttribute("errorMsg", "something wrong on server");
            }
        }

        return "redirect:/register";
    }

//	Forgot Password Code

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot_password.html";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
            throws UnsupportedEncodingException, MessagingException {

        UserDtls userByEmail = userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Invalid email");
        } else {

            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, resetToken);

            // Generate URL :
            // http://localhost:8080/reset-password?token=sfgdbgfswegfbdgfewgvsrg

            String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

            Boolean sendMail = commonUtil.sendMail(url, email);

            if (sendMail) {
                session.setAttribute("succMsg", "Please check your email..Password Reset link sent");
            } else {
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
            // session.setAttribute("succMsg", "Password change successfully");
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

}
