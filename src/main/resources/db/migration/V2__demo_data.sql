-- Demo data for DSA Learning Platform (supports Vietnamese)
-- WARNING: if your database has different table names or schema, adjust accordingly.

-- Note on passwords: password hashes below are BCrypt hashes for the plaintext `password123`.

-- Insert demo users (students, instructors, admin)
INSERT INTO [dbo].[users] (full_name, date_of_birth, gender, address, email, phone, password_hash, role, created_at, is_active) VALUES
(N'Nguyễn Văn An','1996-04-12',N'Nam',N'Hà Nội',N'an.nguyen@example.com',N'0901234567',N'$2a$10$CwTycUXWue0Thq9StjUM0uJ8Lr4S.1p2b4Q6F8/1O8p3tK/e1bq2.', N'CUSTOMER', SYSUTCDATETIME(), 1),
(N'Trần Thị Bích','1998-11-30',N'Nữ',N'Hồ Chí Minh',N'bich.tran@example.com',N'0912345678',N'$2a$10$CwTycUXWue0Thq9StjUM0uJ8Lr4S.1p2b4Q6F8/1O8p3tK/e1bq2.', N'CUSTOMER', SYSUTCDATETIME(), 1),
(N'Lê Văn Cường','1985-07-20',N'Nam',N'Đà Nẵng',N'cuong.le@example.com',N'0987654321',N'$2a$10$CwTycUXWue0Thq9StjUM0uJ8Lr4S.1p2b4Q6F8/1O8p3tK/e1bq2.', N'INSTRUCTOR', SYSUTCDATETIME(), 1),
(N'Phạm Thị Dung','1990-02-15',N'Nữ',N'Hải Phòng',N'dung.pham@example.com',N'0978123456',N'$2a$10$CwTycUXWue0Thq9StjUM0uJ8Lr4S.1p2b4Q6F8/1O8p3tK/e1bq2.', N'INSTRUCTOR', SYSUTCDATETIME(), 1),
(N'Admin Hệ Thống','1980-01-01',N'Nam',N'Hà Nội',N'admin@example.com',N'0900000000',N'$2a$10$CwTycUXWue0Thq9Stq9StjUM0uJ8Lr4S.1p2b4Q6F8/1O8p3tK/e1bq2.', N'ADMIN', SYSUTCDATETIME(), 1);

-- Insert demo courses (in Vietnamese)
INSERT INTO [dbo].[course] (title, description, price, instructor_id, created_at, is_published) VALUES
(N'Nhập môn cấu trúc dữ liệu', N'Khóa học giới thiệu các khái niệm cơ bản về cấu trúc dữ liệu: mảng, danh sách liên kết, ngăn xếp, hàng đợi.', 0.00, 3, SYSUTCDATETIME(), 1),
(N'Thuật toán nâng cao và tối ưu hoá', N'Các kỹ thuật thuật toán (quy hoạch động, tham lam, tìm kiếm và sắp xếp nâng cao, đồ thị).', 199.00, 3, SYSUTCDATETIME(), 1),
(N'Luyện tập giải bài tập DSA cho phỏng vấn', N'Bộ bài tập thực hành có lời giải và test cases, thích hợp ôn luyện phỏng vấn kỹ thuật.', 99.00, 3, SYSUTCDATETIME(), 1);

-- Enrollments: (students enroll to courses)
INSERT INTO [dbo].[enrollment] (student_id, course_id, enrolled_at, progress) VALUES
((SELECT user_id FROM [dbo].[users] WHERE email=N'an.nguyen@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Nhập môn cấu trúc dữ liệu'), SYSUTCDATETIME(), 20),
((SELECT user_id FROM [dbo].[users] WHERE email=N'bich.tran@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Thuật toán nâng cao và tối ưu hoá'), SYSUTCDATETIME(), 5);

-- Payments (student purchases)
INSERT INTO [dbo].[payment] (student_id, course_id, amount, provider, paid_at) VALUES
((SELECT user_id FROM [dbo].[users] WHERE email=N'an.nguyen@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Nhập môn cấu trúc dữ liệu'), 0.00, N'free', SYSUTCDATETIME()),
((SELECT user_id FROM [dbo].[users] WHERE email=N'bich.tran@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Thuật toán nâng cao và tối ưu hoá'), 199.00, N'mock_gateway', SYSUTCDATETIME());

-- Submissions (students submit code)
INSERT INTO [dbo].[submission] (student_id, course_id, code, score, submitted_at) VALUES
((SELECT user_id FROM [dbo].[users] WHERE email=N'an.nguyen@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Nhập môn cấu trúc dữ liệu'), N'public class HelloWorld { public static void main(String[] args){ System.out.println(N''Xin chào, DSA!''); } }', 85, SYSUTCDATETIME()),
((SELECT user_id FROM [dbo].[users] WHERE email=N'bich.tran@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Thuật toán nâng cao và tối ưu hoá'), N'// Giải thuật DFS cơ bản\nvoid dfs(int u){ /* ... */ }', 72, SYSUTCDATETIME());

-- Progress events (learning tracking)
INSERT INTO [dbo].[progress_event] (student_id, course_id, event_type, value, event_at) VALUES
((SELECT user_id FROM [dbo].[users] WHERE email=N'an.nguyen@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Nhập môn cấu trúc dữ liệu'), N'watch_video', 1, SYSUTCDATETIME()),
((SELECT user_id FROM [dbo].[users] WHERE email=N'an.nguyen@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Nhập môn cấu trúc dữ liệu'), N'complete_quiz', 50, SYSUTCDATETIME()),
((SELECT user_id FROM [dbo].[users] WHERE email=N'bich.tran@example.com'), (SELECT course_id FROM [dbo].[course] WHERE title=N'Thuật toán nâng cao và tối ưu hoá'), N'view_solution', 1, SYSUTCDATETIME());

-- Optional: mark some courses as published/unpublished
UPDATE [dbo].[course] SET is_published = 1 WHERE title IN (N'Nhập môn cấu trúc dữ liệu',N'Thuật toán nâng cao và tối ưu hoá',N'Luyện tập giải bài tập DSA cho phỏng vấn');

-- Done demo data
PRINT N'Demo data (V2) inserted';
