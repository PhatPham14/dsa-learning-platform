# DSA Learning Platform - Project Documentation

This document provides an overview of the DSA Learning Platform project, including the technologies used and details of the main business flows that have been implemented and updated as per requirements.

## 1. Introduction
DSA Learning Platform is an online learning web platform that allows Instructors to publish courses and Students to enroll in them. The system is built with a Server-side Rendering architecture using Spring Boot and Thymeleaf.

## 2. Technologies Used
- **Backend**: Java 21, Spring Boot 3.5.x
- **Frontend**: Thymeleaf (Template Engine), HTML5, CSS3, JavaScript
- **Database**: SQL Server
- **Build Tool**: Maven

## 3. System Login Accounts
- Admin:
  - Email: admin@example.com
  - Password: 123456
- Instructor:
  - Email: cuong.le@example.com
  - Password: 123456
- Student:
  - Email: phatpddse182630@fpt.edu.vn
  - Password: 123456

## 4. Main Business Flows

Below are detailed descriptions of the key functional flows that have been implemented and refined:

### 4.1. Course Approval Workflow
To ensure content quality, courses must be approved by an Admin before being publicly displayed.

1.  **Instructor Creates Course**:
    -   Instructor logs in and navigates to the "Create Course" page.
    -   Fills in information and uploads an image.
    -   After creation, the course defaults to **`isActive = false`** status (Inactive).
    -   The course does **not** appear on the public course list (`/courses`) at this stage.

2.  **Admin Approval**:
    -   Admin logs in to the **Admin Dashboard**.
    -   In the "Pending Courses" section, the Admin sees a list of newly created courses.
    -   Admin clicks the **"Approve"** button.
    -   The system updates the course status to **`isActive = true`**.

3.  **Display**:
    -   Once approved, the course officially appears on the home page and the course list page for all students.

### 4.2. Payment & Enrollment Flow
This process ensures that students can only access course content after "purchasing" the course.

1.  **Student Selects Course**:
    -   Student views the course list (`/courses`).
    -   Unpurchased courses display an **"Enroll Now"** button.

2.  **Purchase Execution**:
    -   Clicking "Enroll Now" redirects the student to the payment confirmation page (`/purchase`).
    -   System checks:
        -   Is the student logged in?
        -   Does the student already own this course? (Prevents duplicate purchases).
        -   Is the student the Instructor of this specific course? (Prevents self-purchasing).

3.  **Result**:
    -   After successful payment confirmation, the system creates an **`Enrollment`** record in the database.
    -   The button on the interface changes to **"Enrolled"**.
    -   The course appears in the student's **"My Courses"** page.

### 4.3. Admin Dashboard Workflow
This is the central administration area where the Admin controls key system activities.

1.  **Access & Security**:
    -   Path: `/admin/dashboard`.
    -   **Authorization**: Only accounts with the `ADMIN` role are allowed access. The system blocks unauthorized access from Students or Instructors.

2.  **"Pending Courses" Function**:
    -   **Display**: List of all courses newly created by Instructors but not yet active (`isActive = false`).
    -   **Information**: Shows details like Course Title, Instructor, and Price.
    -   **Action**: The **"Approve"** button allows the Admin to quickly approve a course. Upon approval, the course vanishes from the pending list and appears on the public site.

3.  **Revenue & Statistics Reports**:
    -   **Overview**: Displays Total Platform Revenue and Platform Earnings (10% commission).
    -   **Details**:
        -   **Best Selling**: Top-selling courses of the month.
        -   **Revenue Trend**: Chart/Table comparing revenue over the last 3 months.
        -   **Revenue by Course**: Detailed revenue breakdown by course.
        -   **Transaction History**: Full transaction history, clearly showing the split between Instructor Share (90%) and Platform Share (10%).

4.  **Future Expansion**:
    -   The Dashboard is designed to easily integrate User Management (Ban/Unban) and more in-depth reporting modules in the future.

## 5. Installation & Run Guide
1.  **Database Configuration**:
    -   Open `src/main/resources/application.properties`.
    -   Ensure the Connection String matches your SQL Server instance.
    -   Note the setting: `spring.jpa.hibernate.ddl-auto=update` to automatically generate tables.
    -   Open `src/main/resources/data.sql` to view the database structure and import sample data.

2.  **Run Application**:
    -   Open a terminal in the project root directory.
    -   Run command: `mvn spring-boot:run`.
    -   Access: `http://localhost:8080`.

