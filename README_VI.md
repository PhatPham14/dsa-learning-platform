# DSA Learning Platform - Tài Liệu Dự Án

Đây là tài liệu mô tả tổng quan về dự án DSA Learning Platform, bao gồm các công nghệ sử dụng và chi tiết các luồng nghiệp vụ chính (Main Flows) đã được triển khai và cập nhật theo yêu cầu.

## 1. Giới Thiệu
DSA Learning Platform là một nền tảng web học trực tuyến, cho phép giảng viên (Instructor) đăng tải khóa học và học viên (Student) đăng ký học tập. Hệ thống được xây dựng với kiến trúc Server-side Rendering sử dụng Spring Boot và Thymeleaf.

## 2. Công Nghệ Sử Dụng
- **Backend**: Java 21, Spring Boot 3.5.x
- **Frontend**: Thymeleaf (Template Engine), HTML5, CSS3, JavaScript
- **Database**: SQL Server
- **Build Tool**: Maven

## 3. Các accounts đăng nhập hệ thống
- Admin:
  - Email:admin@example.com
  - Password: 123456
- Instructor:
  - Email:cuong.le@example.com
  - Password: 123456
- Student:
  - Email:phatpddse182630@fpt.edu.vn
  - Password: 123456

## 4. Các Luồng Nghiệp Vụ Chính (Main Flows)

Dưới đây là mô tả chi tiết các luồng chức năng quan trọng đã được thực hiện và tinh chỉnh:

### 4.1. Quy Trình Duyệt Khóa Học (Course Approval Workflow)
Để đảm bảo chất lượng nội dung, khóa học cần được Admin xét duyệt trước khi hiển thị công khai.

1.  **Instructor tạo khóa học**:
    -   Giảng viên đăng nhập, vào trang "Create Course".
    -   Điền thông tin và upload ảnh.
    -   Sau khi tạo, khóa học sẽ có trạng thái mặc định là **`isActive = false`** (Chưa kích hoạt).
    -   Khóa học lúc này **không** xuất hiện trên trang danh sách chung (`/courses`).

2.  **Admin phê duyệt**:
    -   Admin đăng nhập vào **Admin Dashboard**.
    -   Tại phần "Pending Courses" (Khóa học chờ duyệt), Admin sẽ thấy danh sách các khóa học mới tạo.
    -   Admin nhấn nút **"Approve"** (Duyệt).
    -   Hệ thống cập nhật trạng thái khóa học thành **`isActive = true`**.

3.  **Hiển thị**:
    -   Sau khi được duyệt, khóa học sẽ xuất hiện chính thức trên trang chủ và trang danh sách khóa học cho tất cả học viên.


### 4.2. Quy Trình Thanh Toán & Đăng Ký (Payment & Enrollment Flow)
Quy trình đảm bảo học viên chỉ học được khi đã "mua" khóa học.

1.  **Học viên chọn khóa học**:
    -   Học viên xem danh sách khóa học (`/courses`).
    -   Các khóa học chưa mua sẽ hiện nút **"Enroll Now"**.

2.  **Thực hiện mua (Purchase)**:
    -   Khi nhấn "Enroll Now", học viên được chuyển đến trang xác nhận thanh toán (`/purchase`).
    -   Hệ thống kiểm tra:
        -   Học viên đã đăng nhập chưa?
        -   Học viên đã sở hữu khóa học này chưa? (Tránh mua trùng).
        -   Học viên có phải là Instructor của chính khóa học này không? (Chặn tự mua khóa học của mình).

3.  **Kết quả**:
    -   Sau khi xác nhận thanh toán thành công, hệ thống tạo bản ghi **`Enrollment`** trong database.
    -   Trạng thái nút bấm trên giao diện chuyển thành **"Enrolled"** (Đã đăng ký).
    -   Khóa học xuất hiện trong trang **"My Courses"** của học viên.

### 4.3. Luồng Admin Dashboard (Admin Dashboard Workflow)
Đây là khu vực quản trị trung tâm, nơi Admin kiểm soát các hoạt động quan trọng của hệ thống.

1.  **Truy cập & Bảo mật**:
    -   Đường dẫn: `/admin/dashboard`.
    -   **Phân quyền**: Chỉ tài khoản có vai trò `ADMIN` mới được phép truy cập. Hệ thống sẽ chặn các truy cập trái phép từ Student hoặc Instructor.

2.  **Chức năng "Pending Courses" (Duyệt Khóa Học)**:
    -   **Hiển thị**: Danh sách tất cả các khóa học mới được tạo bởi Instructor nhưng chưa được kích hoạt (`isActive = false`).
    -   **Thông tin**: Hiển thị chi tiết Tên khóa học, Instructor, và Giá.
    -   **Hành động**: Nút **"Approve"** cho phép Admin duyệt nhanh khóa học. Sau khi duyệt, khóa học biến mất khỏi danh sách chờ và xuất hiện trên trang public.

3.  **Thống Kê & Báo Cáo Doanh Thu (Revenue & Statistics)**:
    -   **Tổng quan**: Hiển thị Tổng doanh thu toàn sàn và Doanh thu của nền tảng (10% hoa hồng).
    -   **Chi tiết**:
        -   **Best Selling**: Top khóa học bán chạy nhất trong tháng.
        -   **Revenue Trend**: Biểu đồ/Bảng so sánh doanh thu 3 tháng gần nhất.
        -   **Revenue by Course**: Doanh thu chi tiết theo từng khóa học.
        -   **Transaction History**: Lịch sử giao dịch đầy đủ, hiển thị rõ phần chia sẻ giữa Instructor (90%) và Platform (10%).

4.  **Mở rộng (Future)**:
    -   Dashboard được thiết kế để dễ dàng tích hợp thêm module quản lý User (Ban/Unban) và các báo cáo chuyên sâu hơn.

## 5. Hướng Dẫn Cài Đặt & Chạy
1.  **Cấu hình Database**:
    -   Mở file `src/main/resources/application.properties`.
    -   Đảm bảo Connection String đúng với SQL Server của bạn.
    -   Lưu ý setting: `spring.jpa.hibernate.ddl-auto=update` để tự động tạo bảng.
    -   Mở file `src/main/resources/data.sql` để xem cấu trúc database và import dữ liệu mẫu.

2.  **Chạy Ứng Dụng**:
    -   Mở terminal tại thư mục gốc dự án.
    -   Chạy lệnh: `mvn spring-boot:run`.
    -   Truy cập: `http://localhost:8080`.

