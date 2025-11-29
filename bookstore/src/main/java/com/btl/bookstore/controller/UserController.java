package com.btl.bookstore.controller;

import com.btl.bookstore.model.*;
import com.btl.bookstore.service.CartService;
import com.btl.bookstore.service.CategoryService;
import com.btl.bookstore.service.OrderService;
import com.btl.bookstore.service.UserService;
import com.btl.bookstore.util.CommonUtil;
import com.btl.bookstore.util.OrderStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Controller cho user
Xử lý giỏ hàng, đặt hàng và quản lý profile
*/
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "user/home";
    }

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

    @GetMapping("/addCart")
    public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
        Cart saveCart = cartService.saveCart(pid, uid);

        if (ObjectUtils.isEmpty(saveCart)) {
            session.setAttribute("errorMsg", "Book add to cart failed");
        } else {
            session.setAttribute("succMsg", "Book added to cart");
        }
        return "redirect:/book/" + pid;
    }

    @GetMapping("/cart")
    public String loadCartPage(Principal p, Model m) {

        UserDtls user = getLoggedInUserDetails(p);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        m.addAttribute("carts", carts);
        if (carts.size() > 0) {
            Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            m.addAttribute("totalOrderPrice", totalOrderPrice);
        }
        return "user/cart";
    }

    // Cập nhật các item được chọn trong giỏ hàng
    @PostMapping("/updateCartSelection")
    @ResponseBody
    public String updateCartSelection(@RequestBody List<Integer> selectedIds, Principal p) {
        UserDtls user = getLoggedInUserDetails(p);
        cartService.updateCartSelection(selectedIds, user.getId());
        return "success";
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng (tăng/giảm hoặc nhập trực tiếp)
    @GetMapping("/cartQuantityUpdate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartQuantity(
            @RequestParam Integer cid, 
            @RequestParam(required = false) String sy,
            @RequestParam(required = false) Integer qty,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Cart updatedCart;
            if (qty != null) {
                updatedCart = cartService.updateQuantityDirect(cid, qty);
            } else if (sy != null) {
                updatedCart = cartService.updateQuantity(sy, cid);
            } else {
                response.put("success", false);
                response.put("message", "Missing parameters");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("itemTotal", updatedCart.getTotalPrice());
            response.put("quantity", updatedCart.getQuantity());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Số lượng vượt quá kho! " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private UserDtls getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        UserDtls userDtls = userService.getUserByEmail(email);
        return userDtls;
    }

    // Trang thanh toán - tính tổng tiền, phí ship, thuế
    @GetMapping("/orders")
    public String orderPage(Principal p, Model m) {
        UserDtls user = getLoggedInUserDetails(p);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        
        List<Cart> selectedCarts = carts.stream()
                .filter(cart -> cart.getSelected() != null && cart.getSelected())
                .toList();
        
        m.addAttribute("carts", selectedCarts);
        if (selectedCarts.size() > 0) {
            Double orderPrice = selectedCarts.stream()
                    .mapToDouble(cart -> cart.getTotalPrice())
                    .sum();
            Double deliveryFee = 2500.0;
            Double tax = orderPrice * 0.05; // 5% của subtotal
            Double totalOrderPrice = orderPrice + deliveryFee + tax;
            m.addAttribute("orderPrice", orderPrice);
            m.addAttribute("deliveryFee", deliveryFee);
            m.addAttribute("tax", tax);
            m.addAttribute("totalOrderPrice", totalOrderPrice);
        }
        return "user/order";
    }

    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute OrderRequest request, Principal p, HttpSession session) throws Exception {
        UserDtls user = getLoggedInUserDetails(p);
        
        // Check if cart has sufficient stock before saving order
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        
        for (Cart cart : carts) {
            // Chỉ kiểm tra các items đã được chọn
            if (cart.getSelected() != null && cart.getSelected()) {
                if (cart.getBook().getStock() < cart.getQuantity()) {
                    session.setAttribute("errorMsg", "Không đủ hàng cho sách '" + cart.getBook().getTitle() + 
                        "'. Còn lại: " + cart.getBook().getStock() + ", Yêu cầu: " + cart.getQuantity());
                    return "redirect:/user/orders";
                }
            }
        }
        
        orderService.saveOrder(user.getId(), request);
        return "redirect:/user/success";
    }

    @GetMapping("/success")
    public String loadSuccess() {
        return "user/success";
    }

    @GetMapping("/user-orders")
    public String myOrder(Model m, Principal p) {
        UserDtls loginUser = commonUtil.getLoggedInUserDetails(p);
        List<BookOrder> orders = orderService.getOrdersByUser(loginUser.getId());
        m.addAttribute("orders", orders);
        return "user/my_orders";
    }

    @GetMapping("/update-status")
    public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

        OrderStatus[] values = OrderStatus.values();
        String status = null;

        for (OrderStatus orderSt : values) {
            if (orderSt.getId().equals(st)) {
                status = orderSt.getName();
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
            session.setAttribute("errorMsg", "status not updated");
        }
        return "redirect:/user/user-orders";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
        UserDtls updateUserProfile = userService.updateUserProfile(user, img);
        if (ObjectUtils.isEmpty(updateUserProfile)) {
            session.setAttribute("errorMsg", "Profile not updated");
        } else {
            session.setAttribute("succMsg", "Profile Updated");
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
                                 HttpSession session) {
        UserDtls loggedInUserDetails = getLoggedInUserDetails(p);

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

        return "redirect:/user/profile";
    }

}
