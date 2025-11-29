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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.btl.bookstore.model.ChatMessage;
import com.btl.bookstore.model.ChatNotification;
import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.repository.UserRepository;
import com.btl.bookstore.service.ChatMessageService;

/*
Controller xử lý chat realtime
Quản lý tin nhắn giữa user và admin qua WebSocket
*/
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserRepository userRepository;

    // Xử lý tin nhắn chat qua WebSocket
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        ChatMessage saved = chatMessageService.save(chatMessage);

        // Gửi thông báo cho người nhận
        ChatNotification notification = new ChatNotification();
        notification.setId(saved.getId());
        notification.setSenderId(saved.getSender().getId());
        notification.setSenderName(saved.getSender().getName());
        notification.setContent(saved.getContent());
        notification.setTimestamp(saved.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        messagingTemplate.convertAndSendToUser(
            saved.getReceiver().getId().toString(),
            "/queue/messages",
            notification
        );
    }

    // Lấy danh sách tin nhắn trong phòng chat
    @GetMapping("/chat/messages/{chatRoomId}")
    @ResponseBody
    public List<Map<String, Object>> getChatMessages(@PathVariable String chatRoomId, Principal principal) {
        List<ChatMessage> messages = chatMessageService.findChatMessages(chatRoomId);
        List<Map<String, Object>> result = new ArrayList<>();

        String currentUserEmail = principal.getName();
        UserDtls currentUser = userRepository.findByEmail(currentUserEmail);

        // Đánh dấu tin nhắn là đã đọc
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
        Map<String, Long> result = new HashMap<>();
        
        if (principal == null) {
            result.put("count", 0L);
            return result;
        }
        
        String currentUserEmail = principal.getName();
        UserDtls currentUser = userRepository.findByEmail(currentUserEmail);
        
        if (currentUser == null) {
            result.put("count", 0L);
            return result;
        }

        Long count = chatMessageService.countUnreadMessages(currentUser.getId());
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

        List<String> chatRooms = chatMessageService.findUserChatRooms(currentUser.getId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (String chatRoomId : chatRooms) {
            String[] userIds = chatRoomId.split("_");
            Integer otherUserId = Integer.parseInt(userIds[0]);
            if (otherUserId.equals(currentUser.getId())) {
                otherUserId = Integer.parseInt(userIds[1]);
            }

            UserDtls otherUser = userRepository.findById(otherUserId).orElse(null);
            if (otherUser != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", otherUser.getId());
                userData.put("name", otherUser.getName());
                userData.put("email", otherUser.getEmail());
                userData.put("chatRoomId", chatRoomId);
                userData.put("unreadCount", chatMessageService.countUnreadMessagesInChatRoom(chatRoomId, currentUser.getId()));
                result.add(userData);
            }
        }

        return result;
    }
}

