package com.btl.bookstore.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.btl.bookstore.model.ChatMessage;
import com.btl.bookstore.repository.ChatMessageRepository;
import com.btl.bookstore.service.ChatMessageService;

import jakarta.transaction.Transactional;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setIsRead(false);
        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> findChatMessages(String chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }

    @Override
    public List<String> findUserChatRooms(Integer userId) {
        return chatMessageRepository.findDistinctChatRoomsByUserId(userId);
    }

    @Override
    public List<String> findAllUserChatRooms() {
        return chatMessageRepository.findAllDistinctChatRooms();
    }

    @Override
    public Long countUnreadMessages(Integer userId) {
        return chatMessageRepository.countUnreadMessagesByReceiverId(userId);
    }

    @Override
    public Long countUnreadMessagesInChatRoom(String chatRoomId, Integer userId) {
        return chatMessageRepository.countUnreadMessagesByChatRoomAndReceiver(chatRoomId, userId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String chatRoomId, Integer userId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
        for (ChatMessage message : messages) {
            if (message.getReceiver().getId().equals(userId) && !message.getIsRead()) {
                message.setIsRead(true);
                chatMessageRepository.save(message);
            }
        }
    }

    @Override
    public String createChatRoomId(Integer userId1, Integer userId2) {
        // Always put smaller ID first for consistency
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }
}

