let stompClient = null;
let currentUserId = null;
let selectedUser = null;
let currentChatRoomId = null;
let chatButton, chatWindow, chatClose, userList, chatMessages, chatInput, chatSend, chatBadge;

function initializeElements() {
    chatButton = document.getElementById('chatButton');
    chatWindow = document.getElementById('chatWindow');
    chatClose = document.getElementById('chatClose');
    userList = document.getElementById('userList');
    chatMessages = document.getElementById('chatMessages');
    chatInput = document.getElementById('chatInput');
    chatSend = document.getElementById('chatSend');
    chatBadge = document.getElementById('chatBadge');

    chatButton.addEventListener('click', function() {
        chatWindow.classList.toggle('show');
        if (chatWindow.classList.contains('show')) {
            loadUserList();
        }
    });

    chatClose.addEventListener('click', function() {
        chatWindow.classList.remove('show');
    });

    chatSend.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}

function sendMessage() {
    const content = chatInput.value.trim();
    if (content && selectedUser && stompClient) {
        const chatMessage = {
            sender: { id: currentUserId },
            receiver: { id: selectedUser.id },
            content: content,
            chatRoomId: currentChatRoomId
        };

        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        chatInput.value = '';

        addMessageToUI({
            content: content,
            timestamp: new Date().toLocaleString('vi-VN'),
            isOwn: true
        });
    }
}

function addMessageToUI(message) {
    const emptyChat = chatMessages.querySelector('.empty-chat');
    if (emptyChat) {
        emptyChat.remove();
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = 'chat-message ' + (message.isOwn ? 'own' : 'other');

    let html = '';
    if (!message.isOwn) {
        html += '<div class="message-sender">' + (message.senderName || 'User') + '</div>';
    }
    html += '<div class="message-content">' + escapeHtml(message.content) + '</div>';
    html += '<div class="message-time">' + message.timestamp + '</div>';

    messageDiv.innerHTML = html;
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function loadUserList() {
    fetch('/admin/chat/user-list')
        .then(response => response.json())
        .then(users => {
            if (users.length === 0) {
                userList.innerHTML = '<div class="empty-user-list">Chưa có tin nhắn từ user nào</div>';
            } else {
                userList.innerHTML = '';
                users.forEach(user => {
                    const userDiv = document.createElement('div');
                    userDiv.className = 'user-item';
                    userDiv.dataset.userId = user.id;
                    userDiv.dataset.chatRoomId = user.chatRoomId;

                    let html = '<div class="user-item-name">' + escapeHtml(user.name) + '</div>';
                    html += '<div class="user-item-email">' + escapeHtml(user.email) + '</div>';
                    if (user.unreadCount > 0) {
                        html += '<span class="user-unread-badge">' + user.unreadCount + '</span>';
                    }

                    userDiv.innerHTML = html;
                    userDiv.addEventListener('click', function() {
                        selectUser({
                            id: user.id,
                            name: user.name,
                            email: user.email,
                            chatRoomId: user.chatRoomId
                        });
                    });

                    userList.appendChild(userDiv);
                });
            }
        });
}

function selectUser(user) {
    selectedUser = user;
    currentChatRoomId = user.chatRoomId;

    document.querySelectorAll('.user-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`.user-item[data-user-id="${user.id}"]`).classList.add('active');

    chatInput.disabled = false;
    chatSend.disabled = false;
    chatInput.focus();

    loadMessages();
}

function loadMessages() {
    if (currentChatRoomId) {
        fetch('/chat/messages/' + currentChatRoomId)
            .then(response => response.json())
            .then(messages => {
                chatMessages.innerHTML = '';
                if (messages.length === 0) {
                    chatMessages.innerHTML = '<div class="empty-chat">Bắt đầu cuộc trò chuyện...</div>';
                } else {
                    messages.forEach(msg => {
                        addMessageToUI(msg);
                    });
                }
                updateUnreadCount();
                loadUserList();
            });
    }
}

function updateUnreadCount() {
    fetch('/chat/unread-count')
        .then(response => response.json())
        .then(data => {
            if (data.count > 0) {
                chatBadge.textContent = data.count;
                chatBadge.classList.add('show');
            } else {
                chatBadge.classList.remove('show');
            }
        });
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/user/' + currentUserId + '/queue/messages', function(message) {
            const notification = JSON.parse(message.body);

            if (chatWindow.classList.contains('show') && selectedUser && selectedUser.id === notification.senderId) {
                addMessageToUI({
                    content: notification.content,
                    timestamp: notification.timestamp,
                    senderName: notification.senderName,
                    isOwn: false
                });
            } else {
                updateUnreadCount();
                if (chatWindow.classList.contains('show')) {
                    loadUserList();
                }
            }
        });
    });
}

function initChat(userId) {
    currentUserId = userId;
    initializeElements();
    if (currentUserId) {
        connect();
        updateUnreadCount();
        setInterval(updateUnreadCount, 30000);
    }
}

