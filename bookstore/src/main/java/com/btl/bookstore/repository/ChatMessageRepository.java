package com.btl.bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.btl.bookstore.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);

    @Query("SELECT DISTINCT c.chatRoomId FROM ChatMessage c WHERE c.sender.id = ?1 OR c.receiver.id = ?1")
    List<String> findDistinctChatRoomsByUserId(Integer userId);

    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.receiver.id = ?1 AND c.isRead = false")
    Long countUnreadMessagesByReceiverId(Integer receiverId);

    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.chatRoomId = ?1 AND c.receiver.id = ?2 AND c.isRead = false")
    Long countUnreadMessagesByChatRoomAndReceiver(String chatRoomId, Integer receiverId);
}

