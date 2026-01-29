package com.edu.dsalearningplatform.repository;

import com.edu.dsalearningplatform.dto.InstructorPaymentDTO;
import com.edu.dsalearningplatform.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT new com.edu.dsalearningplatform.dto.InstructorPaymentDTO(c.title, s.fullName, p.paidAt, p.amount, p.instructorShare, p.adminShare) " +
           "FROM Payment p JOIN p.course c JOIN p.student s JOIN c.instructor i WHERE i.userId = :instructorId")
    List<InstructorPaymentDTO> findByCourse_Instructor_UserId(@Param("instructorId") Integer instructorId);

    @Query("SELECT new com.edu.dsalearningplatform.dto.InstructorPaymentDTO(c.title, s.fullName, p.paidAt, p.amount, p.instructorShare, p.adminShare) " +
           "FROM Payment p JOIN p.course c JOIN p.student s")
    List<InstructorPaymentDTO> findAllPaymentsForAdmin();

    @Query("SELECT p.course.title, COUNT(p) as salesCount FROM Payment p WHERE p.paidAt BETWEEN :startDate AND :endDate GROUP BY p.course.title ORDER BY salesCount DESC")
    List<Object[]> findBestSellingCourses(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT YEAR(p.paidAt), MONTH(p.paidAt), SUM(p.adminShare) FROM Payment p WHERE p.paidAt >= :startDate GROUP BY YEAR(p.paidAt), MONTH(p.paidAt) ORDER BY YEAR(p.paidAt) DESC, MONTH(p.paidAt) DESC")
    List<Object[]> findMonthlyRevenue(@Param("startDate") java.time.LocalDateTime startDate);
}
