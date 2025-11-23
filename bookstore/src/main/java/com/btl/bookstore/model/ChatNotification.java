package com.btl.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatNotification {
    private Long id;
    private Integer senderId;
    private String senderName;
    private String content;
    private String timestamp;
}

