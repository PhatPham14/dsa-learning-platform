package com.edu.dsalearningplatform.enums;

public enum NotificationType {
    COURSE_CREATED,    // INSTRUCTOR tạo khóa học -> thông báo ADMIN
    COURSE_ENABLED,    // ADMIN enable khóa học -> thông báo INSTRUCTOR
    COURSE_DISABLED,   // ADMIN disable khóa học -> thông báo INSTRUCTOR
    STUDENT_ENROLLED   // Học viên mua khóa học -> thông báo INSTRUCTOR
}
