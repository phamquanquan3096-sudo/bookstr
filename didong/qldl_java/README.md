# Quản Lý Đại Lý - Java Spring Boot Backend API

Hệ thống REST API Backend viết bằng **Java (Spring Boot 3.x)** thay thế toàn bộ hệ thống C# ASP.NET MVC cũ.

## 🚀 Tính năng & Endpoints
- **Authentication API**: `/api/v1/auth/login`, `/api/v1/auth/register-daily` (hỗ trợ phân quyền Nhân viên & Đại lý).
- **Quản lý Sản phẩm**: `/api/v1/sanpham` (Tra cứu catalog, tìm kiếm, cập nhật tồn kho).
- **Quản lý Đơn hàng**: `/api/v1/donhang` (Đại lý tạo đơn, Kinh doanh duyệt đơn, Phân công giao hàng).
- **Quản lý Đại lý**: `/api/v1/daily` (Danh sách đại lý, quản lý loại đại lý & hạn mức nợ).
- **Phân hệ Kế toán**: `/api/v1/ketoan/congno` (Theo dõi phiếu công nợ, thu tiền đại lý).
- **Phân hệ Thủ kho**: `/api/v1/kho` (Quản lý tồn kho & nhập/xuất).
- **Phân hệ Giao hàng**: `/api/v1/giaohang` (Nhận đơn được phân công, cập nhật trạng thái giao hàng).

## 🛠 Hướng dẫn chạy
```bash
cd d:\quanly\qldl_java
mvn spring-boot:run
```
Hệ thống sử dụng H2 In-Memory Database (hoặc cấu hình kết nối SQL Server/MySQL trong `application.properties`).
Trang H2 Console: `http://localhost:8080/h2-console`
