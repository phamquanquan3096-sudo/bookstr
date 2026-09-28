# Quản Lý Đại Lý - Android Native Application (Java)

Ứng dụng di động Android viết bằng **Java Native** phục vụ đa vai trò người dùng trong hệ thống Quản Lý Đại Lý.

## 📱 Cấu trúc & Giao diện
- **Giao diện Đăng nhập (`LoginActivity`)**: Đăng nhập phân quyền vai trò (Đại lý, Kinh doanh, Kế toán, Thủ kho, Giao hàng).
- **Trang chủ Navigation (`MainActivity`)**:
  - `SanPhamFragment`: Danh mục sản phẩm, tìm kiếm thời gian thực.
  - `DonHangFragment`: Danh sách đơn hàng, theo dõi trạng thái giao hàng, nút bấm tạo đơn hàng nhanh.
  - `TaoDonHangActivity`: Đặt hàng online trực tiếp dành cho Đại lý.
  - `CongNoFragment`: Theo dõi hạn mức công nợ & số tiền nợ.
  - `KhoFragment`: Tra cứu danh sách kho & hàng tồn.

## ⚙️ Thư viện sử dụng
- **Retrofit 2 + Gson**: Kết nối REST API Java Spring Boot Server.
- **Material Components & CardView**: Giao diện Material Design 3 hiện đại.
- **SwipeRefreshLayout & RecyclerView**: Tải dữ liệu danh sách mượt mà.

## 🛠 Hướng dẫn mở dự án
1. Mở **Android Studio**.
2. Chọn `Open` -> Trỏ đến thư mục `d:\quanly\qldl_android`.
3. Cho phép Gradle Sync và chạy trên Android Emulator hoặc thiết bị thật.
