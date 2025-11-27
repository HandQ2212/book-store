package com.btl.bookstore.controller;

import com.btl.bookstore.model.Book;
import com.btl.bookstore.model.BookOrder;
import com.btl.bookstore.model.Category;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.service.*;
import com.btl.bookstore.util.CommonUtil;
import com.btl.bookstore.util.OrderStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public String index() {
        return "admin/index";
    }

    @GetMapping("/loadAddBook")
    public String loadAddBook(Model m) {
        List<Category> categories = categoryService.getAllCategory();
        m.addAttribute("categories", categories);
        return "admin/add_book";
    }

    @GetMapping("/category")
    public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        // m.addAttribute("categorys", categoryService.getAllCategory());
        Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
        List<Category> categorys = page.getContent();
        m.addAttribute("categorys", categorys);

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, @RequestParam("file")MultipartFile file,
                               HttpSession session) throws IOException {
        try {
            String imageName = "default.jpg";
            
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file, "category");
                imageName = imageUrl;
            }
            
            category.setImageName(imageName);
            Boolean existCategory = categoryService.existCategoryIgnoreCase(category.getName());

            if (existCategory) {
                session.setAttribute("errorMsg", "Category Name already exists");
            } else {
                Category saveCategory = categoryService.saveCategory(category);
                if (ObjectUtils.isEmpty(saveCategory)) {
                    session.setAttribute("errorMsg", "Not saved ! internal server error");
                } else {
                    session.setAttribute("succMsg", "Saved successfully");
                }
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error uploading image: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {
        Boolean deleteCategory = categoryService.deleteCategory(id);

        if (deleteCategory) {
            session.setAttribute("succMsg", "category delete success");
        } else {
            session.setAttribute("errorMsg", "something wrong on server");
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable int id, Model m) {
        m.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/edit_category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
                                 HttpSession session) throws IOException {
        try {
            Category oldCategory = categoryService.getCategoryById(category.getId());
            String imageName = oldCategory.getImageName();

            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file, "category");
                imageName = imageUrl;
            }

            if (!ObjectUtils.isEmpty(category)) {
                oldCategory.setName(category.getName());
                oldCategory.setIsActive(category.getIsActive());
                oldCategory.setImageName(imageName);
            }

            Category updateCategory = categoryService.saveCategory(oldCategory);

            if (!ObjectUtils.isEmpty(updateCategory)) {
                session.setAttribute("succMsg", "Category update success");
            } else {
                session.setAttribute("errorMsg", "something wrong on server");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error uploading image: " + e.getMessage());
        }

        return "redirect:/admin/loadEditCategory/" + category.getId();
    }

    @PostMapping("/saveBook")
    public String saveBook(@ModelAttribute Book book, @RequestParam("file") MultipartFile image,
                           HttpSession session) throws IOException {
        try {
            String imageName = "default.jpg";
            
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image, "books");
                imageName = imageUrl;
            }

            book.setImage(imageName);
            book.setDiscount(0);
            book.setDiscountPrice(book.getPrice());
            Book saveBook = bookService.saveBook(book);

            if (!ObjectUtils.isEmpty(saveBook)) {
                session.setAttribute("succMsg", "Book Saved Successfully");
            } else {
                session.setAttribute("errorMsg", "Something wrong on server");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error uploading image: " + e.getMessage());
        }
        return "redirect:/admin/loadAddBook";
    }

    @GetMapping("/books")
    public String loadViewBook(Model m, @RequestParam(defaultValue = "") String ch,
                               @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        List<Book> books = null;
        if (ch != null && ch.length() > 0) {
            books = bookService.searchBook(ch);
        } else {
            books = bookService.getAllBooks();
        }
        m.addAttribute("books", books);

        Page<Book> page = null;
        if (ch != null && ch.length() > 0) {
            page = bookService.searchBookPagination(pageNo, pageSize, ch);
        } else {
            page = bookService.getAllBooksPagination(pageNo, pageSize);
        }
        m.addAttribute("books", page.getContent());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "admin/books";
    }

    @GetMapping("/deleteBook/{id}")
    public String deleteBook(@PathVariable int id, HttpSession session) {
        Boolean deleteBook = bookService.deleteBook(id);
        if (deleteBook) {
            session.setAttribute("succMsg", "Book delete success");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/editBook/{id}")
    public String editBook(@PathVariable int id, Model m) {
        m.addAttribute("book", bookService.getBookById(id));
        m.addAttribute("categories", categoryService.getAllCategory());
        return "admin/edit_book";
    }

    @PostMapping("/updateBook")
    public String updateBook(@ModelAttribute Book book, @RequestParam("file") MultipartFile image,
                             HttpSession session, Model m) {
        if (book.getDiscount() < 0 || book.getDiscount() > 100) {
            session.setAttribute("errorMsg", "Invalid Discount");
        } else {
            Book updateBook = bookService.updateBook(book, image);
            if (!ObjectUtils.isEmpty(updateBook)) {
                session.setAttribute("succMsg", "Book update success");
            } else {
                session.setAttribute("errorMsg", "Something wrong on server");
            }
        }
        return "redirect:/admin/editBook/" + book.getId();
    }

    @GetMapping("/users")
    public String getAllUsers(Model m, @RequestParam Integer type) {
        List<UserDtls> users = null;
        if (type == 1) {
            users = userService.getUsers("ROLE_USER");
        } else {
            users = userService.getUsers("ROLE_ADMIN");
        }
        m.addAttribute("userType", type);
        m.addAttribute("users", users);
        return "/admin/users";
    }

    @GetMapping("/updateSts")
    public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, @RequestParam Integer type, HttpSession session) {
        Boolean f  = userService.updateAccountStatus(id, status);
        if (f) {
            session.setAttribute("succMsg", "Account Status Updated");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/users?type="+type;
    }

    @GetMapping("/orders")
    public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<BookOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
        m.addAttribute("orders", page.getContent());
        m.addAttribute("srch", false);
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "/admin/orders";
    }

    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {
        OrderStatus[] values = OrderStatus.values();
        String status = null;

        for (OrderStatus orderStatus : values) {
            if (orderStatus.getId().equals(st)) {
                status = orderStatus.getName();
            }
        }

        BookOrder updateOrder = orderService.updateOrderStatus(id, status);

        try {
            commonUtil.sendMailForBookOrder(updateOrder, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!ObjectUtils.isEmpty(updateOrder)) {
            session.setAttribute("succMsg", "Status Updated");
        } else {
            session.setAttribute("errorMsg", "Status not updated");
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/search-order")
    public String searchBook(@RequestParam String orderId, Model m, HttpSession session,
                             @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        if (orderId != null && orderId.length() > 0) {
            BookOrder order = orderService.getOrdersByOrderId(orderId.trim());
            if (ObjectUtils.isEmpty(order)) {
                session.setAttribute("errorMsg", "Incorrect orderId");
                m.addAttribute("orderDtls", null);
            } else {
                m.addAttribute("orderDtls", order);
            }
            m.addAttribute("srch", true);
        } else {
            Page<BookOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
            m.addAttribute("orders", page);
            m.addAttribute("srch", false);

            m.addAttribute("pageNo", page.getNumber());
            m.addAttribute("pageSize", pageSize);
            m.addAttribute("totalElements", page.getTotalElements());
            m.addAttribute("totalPages", page.getTotalPages());
            m.addAttribute("isFirst", page.isFirst());
            m.addAttribute("isLast", page.isLast());
        }
        return "/admin/orders";
    }

    @GetMapping("/add-admin")
    public String loadAdminAdd() {
        return "/admin/add_admin";
    }

    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
            throws IOException {
        try {
            String imageName = "default.jpg";
            
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file, "profile");
                imageName = imageUrl;
            }
            
            user.setProfileImage(imageName);
            UserDtls saveUser = userService.saveAdmin(user);

            if (!ObjectUtils.isEmpty(saveUser)) {
                session.setAttribute("succMsg", "Register successfully");
            } else {
                session.setAttribute("errorMsg", "something wrong on server");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error uploading image: " + e.getMessage());
        }

        return "redirect:/admin/add-admin";
    }

    @GetMapping("/profile")
    public String profile() {
        return "/admin/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
        UserDtls updateUserProfile = userService.updateUserProfile(user, img);
        if (ObjectUtils.isEmpty(updateUserProfile)) {
            session.setAttribute("errorMsg", "Profile not updated");
        } else {
            session.setAttribute("succMsg", "Profile Updated");
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword, Principal p,
                                 HttpSession session) {
        UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
        boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
        if (matches) {
            String encodePassword = passwordEncoder.encode(newPassword);
            loggedInUserDetails.setPassword(encodePassword);
            UserDtls updateUser = userService.updateUser(loggedInUserDetails);
            if (ObjectUtils.isEmpty(updateUser)) {
                session.setAttribute("errorMsg", "Password not updated !! Error in server");
            } else {
                session.setAttribute("succMsg", "Password Updated sucessfully");
            }
        } else {
            session.setAttribute("errorMsg", "Current Password incorrect");
        }

        return "redirect:/admin/profile";
    }

}
