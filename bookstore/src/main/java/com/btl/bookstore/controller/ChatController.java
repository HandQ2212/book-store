package com.btl.bookstore.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.btl.bookstore.model.ChatMessage;
import com.btl.bookstore.model.ChatNotification;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.repository.UserRepository;
import com.btl.bookstore.service.ChatMessageService;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        ChatMessage saved = chatMessageService.save(chatMessage);

        // Send notification to receiver
        ChatNotification notification = new ChatNotification();
        notification.setId(saved.getId());
        notification.setSenderId(saved.getSender().getId());
        notification.setSenderName(saved.getSender().getName());
        notification.setContent(saved.getContent());
        notification.setTimestamp(saved.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        // If sender is user, notify all admins
        if (!saved.getSender().getRole().equals("ROLE_ADMIN")) {
            List<UserDtls> admins = userRepository.findByRole("ROLE_ADMIN");
            for (UserDtls admin : admins) {
                messagingTemplate.convertAndSendToUser(
                    admin.getId().toString(),
                    "/queue/messages",
                    notification
                );
            }
        } else {
            // If sender is admin, notify only the user
            messagingTemplate.convertAndSendToUser(
                saved.getReceiver().getId().toString(),
                "/queue/messages",
                notification
            );
        }
    }

    @GetMapping("/chat/messages/{chatRoomId}")
    @ResponseBody
    public List<Map<String, Object>> getChatMessages(@PathVariable String chatRoomId, Principal principal) {
        List<ChatMessage> messages = chatMessageService.findChatMessages(chatRoomId);
        List<Map<String, Object>> result = new ArrayList<>();

        String currentUserEmail = principal.getName();
        UserDtls currentUser = userRepository.findByEmail(currentUserEmail);

        // Mark messages as read
        chatMessageService.markMessagesAsRead(chatRoomId, currentUser.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (ChatMessage msg : messages) {
            Map<String, Object> msgData = new HashMap<>();
            msgData.put("id", msg.getId());
            msgData.put("content", msg.getContent());
            msgData.put("timestamp", msg.getTimestamp().format(formatter));
            msgData.put("senderId", msg.getSender().getId());
            msgData.put("senderName", msg.getSender().getName());
            msgData.put("isOwn", msg.getSender().getId().equals(currentUser.getId()));
            result.add(msgData);
        }

        return result;
    }

    @GetMapping("/chat/unread-count")
    @ResponseBody
    public Map<String, Long> getUnreadCount(Principal principal) {
        String currentUserEmail = principal.getName();
        UserDtls currentUser = userRepository.findByEmail(currentUserEmail);

        Long count = chatMessageService.countUnreadMessages(currentUser.getId());
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    @GetMapping("/chat/admin-list")
    @ResponseBody
    public List<Map<String, Object>> getAdminList() {
        List<UserDtls> admins = userRepository.findByRole("ROLE_ADMIN");
        List<Map<String, Object>> result = new ArrayList<>();

        for (UserDtls admin : admins) {
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", admin.getId());
            adminData.put("name", admin.getName());
            adminData.put("email", admin.getEmail());
            result.add(adminData);
        }

        return result;
    }

    @GetMapping("/admin/chat/user-list")
    @ResponseBody
    public List<Map<String, Object>> getUserChatList(Principal principal) {
        String currentUserEmail = principal.getName();
        UserDtls currentUser = userRepository.findByEmail(currentUserEmail);

        // Get all chat rooms with users (not admin-to-admin chats)
        List<String> allChatRooms = chatMessageService.findAllUserChatRooms();
        List<Map<String, Object>> result = new ArrayList<>();

        for (String chatRoomId : allChatRooms) {
            // chatRoomId format: "user_{userId}"
            if (chatRoomId.startsWith("user_")) {
                String userIdStr = chatRoomId.substring(5); // Remove "user_" prefix
                try {
                    Integer userId = Integer.parseInt(userIdStr);
                    UserDtls user = userRepository.findById(userId).orElse(null);

                    if (user != null && !user.getRole().equals("ROLE_ADMIN")) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", user.getId());
                        userData.put("name", user.getName());
                        userData.put("email", user.getEmail());
                        userData.put("chatRoomId", chatRoomId);
                        userData.put("unreadCount", chatMessageService.countUnreadMessagesInChatRoom(chatRoomId, currentUser.getId()));
                        result.add(userData);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid chatRoomId
                }
            }
        }

        return result;
    }
}

