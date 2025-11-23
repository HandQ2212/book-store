let stompClient = null;
let currentAdminId = null;
let currentChatRoomId = null;
let currentUserId = null;
let chatButton, chatWindow, chatClose, chatMessages, chatInput, chatSend, chatBadge;

function initializeElements() {
    chatButton = document.getElementById('chatButton');
    chatWindow = document.getElementById('chatWindow');
    chatClose = document.getElementById('chatClose');
    chatMessages = document.getElementById('chatMessages');
    chatInput = document.getElementById('chatInput');
    chatSend = document.getElementById('chatSend');
    chatBadge = document.getElementById('chatBadge');

    // Toggle chat window
    chatButton.addEventListener('click', function() {
        chatWindow.classList.toggle('show');
        if (chatWindow.classList.contains('show')) {
            loadAdminAndMessages();
        }
    });

    chatClose.addEventListener('click', function() {
        chatWindow.classList.remove('show');
    });

    // Send message
    chatSend.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}

function sendMessage() {
    const content = chatInput.value.trim();
    if (content && stompClient) {
        const chatMessage = {
            sender: { id: currentUserId },
            receiver: { id: currentAdminId || 0 }, // Dummy admin ID, will be sent to all admins
            content: content,
            chatRoomId: currentChatRoomId
        };

        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        chatInput.value = '';

        // Add message to UI immediately
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
        html += '<div class="message-sender">' + (message.senderName || 'Admin') + '</div>';
    }
    html += '<div class="message-content">' + escapeHtml(message.content) + '</div>';
    html += '<div class="message-time">' + message.timestamp + '</div>';

    messageDiv.innerHTML = html;
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function loadAdminAndMessages() {
    // Use chatRoomId format: "user_{userId}"
    currentChatRoomId = 'user_' + currentUserId;

    // Get first admin for receiver (backward compatibility)
    fetch('/chat/admin-list')
        .then(response => response.json())
        .then(admins => {
            if (admins.length > 0) {
                currentAdminId = admins[0].id;
            }
            loadMessages();
        });
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

// Connect WebSocket
function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/user/' + currentUserId + '/queue/messages', function(message) {
            const notification = JSON.parse(message.body);
            if (chatWindow.classList.contains('show')) {
                addMessageToUI({
                    content: notification.content,
                    timestamp: notification.timestamp,
                    senderName: notification.senderName,
                    isOwn: false
                });
            } else {
                updateUnreadCount();
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
        // Update unread count every 30 seconds
        setInterval(updateUnreadCount, 30000);
    }
}

