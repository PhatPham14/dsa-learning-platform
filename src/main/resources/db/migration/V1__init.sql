-- Initial schema for DSA Learning Platform (MSSQL)
-- Tables: users, course, enrollment, payment, submission, progress_event

CREATE TABLE [dbo].[users] (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    date_of_birth NVARCHAR(20),
    gender NVARCHAR(10),
    address NVARCHAR(255),
    email NVARCHAR(100) UNIQUE,
    phone NVARCHAR(20) UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    role NVARCHAR(50) NOT NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    is_active BIT DEFAULT 1
);

CREATE TABLE [dbo].[course] (
    course_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX),
    price DECIMAL(18,2) NOT NULL DEFAULT 0,
    instructor_id INT NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    is_published BIT DEFAULT 0,
    CONSTRAINT FK_course_instructor FOREIGN KEY (instructor_id) REFERENCES [dbo].[users](user_id)
);

CREATE TABLE [dbo].[enrollment] (
    enrollment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id INT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    progress INT DEFAULT 0,
    CONSTRAINT FK_enrollment_student FOREIGN KEY (student_id) REFERENCES [dbo].[users](user_id),
    CONSTRAINT FK_enrollment_course FOREIGN KEY (course_id) REFERENCES [dbo].[course](course_id)
);

CREATE TABLE [dbo].[payment] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id INT NOT NULL,
    course_id BIGINT NOT NULL,
    amount DECIMAL(18,2),
    provider NVARCHAR(100),
    paid_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_payment_student FOREIGN KEY (student_id) REFERENCES [dbo].[users](user_id),
    CONSTRAINT FK_payment_course FOREIGN KEY (course_id) REFERENCES [dbo].[course](course_id)
);

CREATE TABLE [dbo].[submission] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id INT NOT NULL,
    course_id BIGINT NOT NULL,
    code NVARCHAR(MAX),
    score INT,
    submitted_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_submission_student FOREIGN KEY (student_id) REFERENCES [dbo].[users](user_id),
    CONSTRAINT FK_submission_course FOREIGN KEY (course_id) REFERENCES [dbo].[course](course_id)
);

CREATE TABLE [dbo].[progress_event] (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id INT NOT NULL,
    course_id BIGINT NOT NULL,
    event_type NVARCHAR(100),
    value INT,
    event_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_progress_student FOREIGN KEY (student_id) REFERENCES [dbo].[users](user_id),
    CONSTRAINT FK_progress_course FOREIGN KEY (course_id) REFERENCES [dbo].[course](course_id)
);
