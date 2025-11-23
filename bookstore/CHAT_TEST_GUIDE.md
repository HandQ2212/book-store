# Hướng dẫn Test chức năng Chat với nhiều Admin

## Chuẩn bị

### 1. Tài khoản cần có:
- **User 1**: Tài khoản khách hàng thường
- **User 2**: Tài khoản khách hàng thường (tùy chọn)
- **Admin 1**: Tài khoản admin đã có sẵn
- **Admin 2**: Tài khoản admin mới thêm vào

### 2. Trình duyệt:
- Sử dụng 2-3 trình duyệt khác nhau hoặc cửa sổ ẩn danh (Incognito)
- Ví dụ: Chrome, Firefox, Edge

## Các bước test

### Bước 1: Tạo Admin mới (nếu chưa có)

1. Đăng nhập với tài khoản Admin hiện tại
2. Vào trang quản lý Admin: `/admin/add-admin`
3. Thêm một admin mới:
   - Email: admin2@example.com
   - Password: Admin@123
   - Name: Admin Mới
   - Mobile: 0901234567
4. Lưu lại

### Bước 2: Test User gửi tin nhắn

**Trình duyệt 1 (User)**
1. Đăng nhập với tài khoản User 1
2. Vào trang chủ hoặc bất kỳ trang nào có chat widget
3. Click vào icon chat (góc dưới bên phải)
4. Gửi tin nhắn: "Xin chào, tôi cần hỗ trợ"

**Kết quả mong đợi:**
- Tin nhắn hiển thị ngay trong chat window của user
- Không có lỗi trong Console (F12)

### Bước 3: Test Admin 1 nhận tin nhắn

**Trình duyệt 2 (Admin 1)**
1. Đăng nhập với tài khoản Admin 1 (admin cũ)
2. Vào trang admin dashboard
3. Quan sát icon chat - phải có badge số (1)
4. Click vào icon chat
5. Xem danh sách user - phải thấy User 1 ở đầu danh sách
6. Click vào User 1 để xem tin nhắn

**Kết quả mong đợi:**
- Admin 1 thấy tin nhắn "Xin chào, tôi cần hỗ trợ" từ User 1
- Badge số hiển thị đúng
- Không có lỗi

### Bước 4: Test Admin 2 nhận tin nhắn (KEY TEST)

**Trình duyệt 3 (Admin 2 - Admin mới)**
1. Đăng nhập với tài khoản Admin 2 (admin mới tạo)
2. Vào trang admin dashboard
3. Quan sát icon chat - phải có badge số (1)
4. Click vào icon chat
5. Xem danh sách user - phải thấy User 1 ở đầu danh sách
6. Click vào User 1 để xem tin nhắn

**Kết quả mong đợi:**
✅ Admin 2 (mới) cũng thấy tin nhắn "Xin chào, tôi cần hỗ trợ" từ User 1
✅ Danh sách user hiển thị giống như Admin 1
✅ Có thể click vào User để xem chi tiết

**Nếu BUG (trước khi fix):**
❌ Admin 2 không thấy user trong danh sách
❌ Hoặc không thấy tin nhắn

### Bước 5: Test Admin 2 trả lời

**Trình duyệt 3 (Admin 2)**
1. Trong chat với User 1, nhập: "Xin chào, Admin 2 ở đây. Tôi có thể giúp gì?"
2. Click Gửi

**Trình duyệt 1 (User)**
1. Quan sát chat window (không cần refresh)

**Kết quả mong đợi:**
✅ User nhận được tin nhắn từ Admin 2 ngay lập tức (qua WebSocket)
✅ Tin nhắn hiển thị tên người gửi: "Admin Mới"

### Bước 6: Test Admin 1 cũng có thể trả lời

**Trình duyệt 2 (Admin 1)**
1. Vẫn trong chat với User 1
2. Có thể thấy tin nhắn Admin 2 vừa gửi (sau khi refresh hoặc load lại messages)
3. Nhập: "Admin 1 cũng ở đây hỗ trợ"
4. Click Gửi

**Trình duyệt 1 (User)**
1. Quan sát chat window

**Kết quả mong đợi:**
✅ User nhận được tin nhắn từ cả Admin 1 và Admin 2
✅ Mỗi tin nhắn hiển thị đúng tên admin gửi

### Bước 7: Test với User thứ 2

**Trình duyệt 4 (User 2 - tùy chọn)**
1. Đăng nhập với tài khoản User 2
2. Mở chat và gửi: "Tôi cần tư vấn sản phẩm"

**Trình duyệt 2 (Admin 1) và Trình duyệt 3 (Admin 2)**
1. Cả 2 admin đều phải nhận được notification
2. Cả 2 admin đều thấy User 2 trong danh sách
3. Cả 2 admin đều có thể trả lời

**Kết quả mong đợi:**
✅ Cả 2 admin đều có thể xem và trả lời tin nhắn từ User 2
✅ Hệ thống hoạt động đồng nhất cho mọi admin

## Các trường hợp cần test thêm

### Test 1: Tin nhắn không bị mất
1. User gửi tin nhắn khi không có admin nào online
2. Admin đăng nhập sau đó
3. Admin phải thấy tin nhắn đã gửi trước đó

### Test 2: Badge đếm đúng
1. User gửi 3 tin nhắn
2. Admin thấy badge số 3
3. Admin mở chat và xem tin nhắn
4. Badge phải về 0

### Test 3: Nhiều conversation đồng thời
1. User 1 chat với admin
2. User 2 chat với admin
3. User 3 chat với admin
4. Admin phải thấy 3 users trong danh sách
5. Admin có thể switch giữa các conversation

### Test 4: Real-time notification
1. User gửi tin nhắn
2. Admin đang online và đang ở trang khác (không mở chat)
3. Badge phải tăng ngay lập tức (không cần refresh)

## Checklist tổng quan

Sau khi fix thành công, tất cả các mục sau phải đạt:

- [ ] Admin mới có thể xem danh sách tất cả user đã chat
- [ ] Admin mới có thể xem tin nhắn từ bất kỳ user nào
- [ ] Admin mới có thể trả lời tin nhắn
- [ ] User nhận được tin nhắn từ bất kỳ admin nào
- [ ] Nhiều admin có thể cùng trả lời một user
- [ ] Badge notification hoạt động đúng cho tất cả admin
- [ ] WebSocket real-time hoạt động cho tất cả admin
- [ ] Không có lỗi JavaScript trong Console
- [ ] Không có lỗi Backend trong logs

## Troubleshooting

### Vấn đề: "Cannot resolve method" trong IDE
- **Nguyên nhân**: IDE cache
- **Giải pháp**: 
  - IntelliJ: File > Invalidate Caches > Invalidate and Restart
  - Hoặc chỉ cần build và chạy, code đã đúng

### Vấn đề: Admin không nhận notification real-time
- **Kiểm tra**: WebSocket connection trong Network tab (F12)
- **Kiểm tra**: Console có lỗi không
- **Giải pháp**: Đảm bảo WebSocket config đúng trong application

### Vấn đề: Tin nhắn cũ không hiển thị
- **Nguyên nhân**: ChatRoomId format đã thay đổi
- **Giải pháp**: Chạy SQL migration script trong `migrate_chat_data.sql`

### Vấn đề: Duplicate tin nhắn
- **Kiểm tra**: Có bao nhiêu WebSocket connection trong Network tab
- **Giải pháp**: Đảm bảo chỉ gọi `initChat()` một lần

## Logs để kiểm tra

### Backend logs cần xem:
```
Connected: STOMP ...  // WebSocket connection thành công
Processing chat message from user X to admin Y
Sending notification to admin 1
Sending notification to admin 2
```

### Frontend Console logs:
```
Connected: STOMP...
Message sent successfully
Message received: {...}
```

## Kết luận

Nếu tất cả các test case trên đều PASS, chức năng chat đã hoạt động đúng với nhiều admin!

