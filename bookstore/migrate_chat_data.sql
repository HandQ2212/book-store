-- Script SQL để migrate dữ liệu chat từ format cũ sang format mới
-- Chỉ chạy nếu bạn có dữ liệu chat cũ cần giữ lại

-- Bước 1: Backup bảng chat_message (quan trọng!)
CREATE TABLE chat_message_backup AS SELECT * FROM chat_message;

-- Bước 2: Xem dữ liệu hiện tại
SELECT chat_room_id, COUNT(*) as message_count
FROM chat_message
GROUP BY chat_room_id;

-- Bước 3: Update chatRoomId sang format mới
-- Format cũ: "userId1_userId2" (ví dụ: "5_1", "6_1")
-- Format mới: "user_{userId}" (ví dụ: "user_5", "user_6")
--
-- Giả sử admin có ID = 1, 2, 3 (các admin thường có ID nhỏ)
-- Và user có ID > 3

-- Option A: Nếu biết chính xác ID của admin
UPDATE chat_message
SET chat_room_id = CONCAT('user_',
    CASE
        WHEN sender_id IN (1, 2, 3) THEN receiver_id  -- Nếu sender là admin, lấy receiver (user)
        ELSE sender_id  -- Nếu sender là user, lấy sender
    END
);

-- Option B: Nếu phân biệt admin/user qua bảng user_dtls
UPDATE chat_message cm
SET chat_room_id = CONCAT('user_',
    CASE
        WHEN (SELECT role FROM user_dtls WHERE id = cm.sender_id) = 'ROLE_ADMIN'
        THEN cm.receiver_id
        ELSE cm.sender_id
    END
);

-- Bước 4: Kiểm tra kết quả sau khi update
SELECT chat_room_id, COUNT(*) as message_count
FROM chat_message
GROUP BY chat_room_id
ORDER BY MAX(timestamp) DESC;

-- Bước 5: Nếu có lỗi, restore từ backup
-- DROP TABLE chat_message;
-- CREATE TABLE chat_message AS SELECT * FROM chat_message_backup;

-- Bước 6: Sau khi đã test và chắc chắn ok, xóa backup
-- DROP TABLE chat_message_backup;


-- ==== HOẶC ĐƠN GIẢN HƠN: XÓA DỮ LIỆU CHAT CŨ ====
-- Nếu dữ liệu chat không quan trọng, có thể xóa hết và bắt đầu mới

-- Xóa tất cả tin nhắn chat cũ
-- TRUNCATE TABLE chat_message;

-- Hoặc xóa có điều kiện (ví dụ: chỉ xóa chat cũ hơn 30 ngày)
-- DELETE FROM chat_message WHERE timestamp < DATE_SUB(NOW(), INTERVAL 30 DAY);

