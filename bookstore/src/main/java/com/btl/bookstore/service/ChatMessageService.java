package com.btl.bookstore.service;

import java.util.List;

import com.btl.bookstore.model.ChatMessage;

public interface ChatMessageService {

    ChatMessage save(ChatMessage chatMessage);

    List<ChatMessage> findChatMessages(String chatRoomId);

    List<String> findUserChatRooms(Integer userId);

    Long countUnreadMessages(Integer userId);

    Long countUnreadMessagesInChatRoom(String chatRoomId, Integer userId);

    void markMessagesAsRead(String chatRoomId, Integer userId);

    String createChatRoomId(Integer userId1, Integer userId2);
}

