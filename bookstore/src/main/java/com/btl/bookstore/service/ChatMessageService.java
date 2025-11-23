package com.btl.bookstore.service;

import java.util.List;

import com.btl.bookstore.model.ChatMessage;

/**
 * Chat message service interface
 */
public interface ChatMessageService {

    ChatMessage save(ChatMessage chatMessage);

    List<ChatMessage> findChatMessages(String chatRoomId);

    List<String> findUserChatRooms(Integer userId);

    /**
     * Find all user chat rooms across all admins
     */
    List<String> findAllUserChatRooms();

    Long countUnreadMessages(Integer userId);

    Long countUnreadMessagesInChatRoom(String chatRoomId, Integer userId);

    void markMessagesAsRead(String chatRoomId, Integer userId);

    String createChatRoomId(Integer userId1, Integer userId2);
}

