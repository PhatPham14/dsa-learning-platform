package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.dto.InstructorPaymentDTO;
import com.edu.dsalearningplatform.dto.VnpayCallbackResult;
import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.Enrollment;
import com.edu.dsalearningplatform.entity.Payment;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.EnrollmentRepository;
import com.edu.dsalearningplatform.repository.PaymentRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
// @ConfigurationProperties(prefix = "vnpay.sandbox")
public class PaymentServiceImpl implements PaymentService {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Value("${vnpay.sandbox.tmn-code:${vnpay.sandbox.vnpTmnCode:${VNPAY_TMN_CODE:}}}")
    private String vnpTmnCode;

    @Value("${vnpay.sandbox.hash-secret:${vnpay.sandbox.vnpHashSecret:${VNPAY_HASH_SECRET:}}}")
    private String vnpHashSecret;

    @Value("${vnpay.sandbox.pay-url:${vnpay.sandbox.vnpPayUrl:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}}")
    private String vnpPayUrl;

    @Value("${vnpay.sandbox.return-url:${vnpay.sandbox.vnpReturnUrl:http://localhost:8081/api/payments/vnpay/return}}")
    private String vnpReturnUrl;

    @Value("${vnpay.sandbox.version:${vnpay.sandbox.vnpVersion:2.1.0}}")
    private String vnpVersion;

    @Value("${vnpay.sandbox.locale:${vnpay.sandbox.vnpLocale:vn}}")
    private String vnpLocale;

    @Value("${vnpay.sandbox.order-type:${vnpay.sandbox.vnpOrderType:other}}")
    private String vnpOrderType;

    public PaymentServiceImpl(PaymentRepository paymentRepository, CourseRepository courseRepository, UserRepository userRepository, EnrollmentRepository enrollmentRepository) {
        this.paymentRepository = paymentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional
    public Payment charge(Integer studentId, Long courseId, String provider) {
        PurchaseContext context = validatePurchase(studentId, courseId);
        Course course = context.course();
        User user = context.user();

        Payment p = new Payment();
        p.setCourse(course);
        p.setStudent(user);
        BigDecimal amount = course.getPrice() == null ? BigDecimal.ZERO : course.getPrice();
        p.setAmount(amount);

        // Calculate shares: Admin 15%, Instructor 85%
        BigDecimal adminShare = amount.multiply(new BigDecimal("0.15"));
        BigDecimal instructorShare = amount.subtract(adminShare);

        p.setAdminShare(adminShare);
        p.setInstructorShare(instructorShare);
        p.setProvider(provider == null ? "mock_gateway" : provider);
        Payment saved = paymentRepository.save(p);

        Enrollment e = new Enrollment();
        e.setCourse(course);
        e.setStudent(user);
        enrollmentRepository.save(e);

        return saved;
    }

    @Override
    public String createVnpayPaymentUrl(Integer studentId, Long courseId, String ipAddress) {
        PurchaseContext context = validatePurchase(studentId, courseId);
        ensureVnpayConfigured();

        Course course = context.course();
        BigDecimal amount = course.getPrice() == null ? BigDecimal.ZERO : course.getPrice();
        String txnRef = generateTxnRef(studentId, courseId);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnpVersion);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan khoa hoc " + courseId + " cho hoc vien " + studentId);
        params.put("vnp_OrderType", vnpOrderType);
        params.put("vnp_Locale", vnpLocale);
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_IpAddr", sanitizeIp(ipAddress));
        params.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_DATE_FORMAT));

        String queryString = buildQueryString(params);
        String secureHash = hmacSha512(vnpHashSecret, queryString);
        return vnpPayUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    @Transactional
    public VnpayCallbackResult processVnpayCallback(Map<String, String> vnpParams) {
        ensureVnpayConfigured();

        Map<String, String> data = new TreeMap<>();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (entry.getKey() != null && entry.getKey().startsWith("vnp_") && entry.getValue() != null) {
                data.put(entry.getKey(), entry.getValue());
            }
        }

        String providedHash = data.remove("vnp_SecureHash");
        data.remove("vnp_SecureHashType");
        String calculatedHash = hmacSha512(vnpHashSecret, buildQueryString(data));

        if (providedHash == null || !providedHash.equalsIgnoreCase(calculatedHash)) {
            return new VnpayCallbackResult(false, false, null, "97", "Invalid signature");
        }

        String txnRef = data.get("vnp_TxnRef");
        ParsedTxn parsedTxn = parseTxnRef(txnRef);
        if (parsedTxn == null) {
            return new VnpayCallbackResult(true, false, null, "01", "Order not found");
        }

        String responseCode = data.getOrDefault("vnp_ResponseCode", "99");
        String transactionStatus = data.getOrDefault("vnp_TransactionStatus", "99");
        if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
            return new VnpayCallbackResult(true, false, parsedTxn.courseId(), "00", "Payment not successful");
        }

        Course course = courseRepository.findById(parsedTxn.courseId()).orElse(null);
        if (course == null) {
            return new VnpayCallbackResult(true, false, parsedTxn.courseId(), "01", "Order not found");
        }

        BigDecimal expectedAmount = course.getPrice() == null ? BigDecimal.ZERO : course.getPrice();
        BigDecimal amountFromGateway = parseGatewayAmount(data.get("vnp_Amount"));
        if (amountFromGateway == null || expectedAmount.compareTo(amountFromGateway) != 0) {
            return new VnpayCallbackResult(true, false, parsedTxn.courseId(), "04", "Invalid amount");
        }

        if (enrollmentRepository.existsByStudentUserIdAndCourseCourseId(parsedTxn.studentId(), parsedTxn.courseId())) {
            return new VnpayCallbackResult(true, true, parsedTxn.courseId(), "00", "Order already confirmed");
        }

        charge(parsedTxn.studentId(), parsedTxn.courseId(), "vnpay");
        return new VnpayCallbackResult(true, true, parsedTxn.courseId(), "00", "Confirm success");
    }

    private PurchaseContext validatePurchase(Integer studentId, Long courseId) {
        if (enrollmentRepository.existsByStudentUserIdAndCourseCourseId(studentId, courseId)) {
            throw new RuntimeException("User already enrolled in this course");
        }

        Course course = courseRepository.findById(courseId).orElseThrow();

        if (!course.isActive()) {
            throw new RuntimeException("Cannot purchase an inactive course");
        }

        if (course.getInstructor() != null && course.getInstructor().getUserId().equals(studentId)) {
            throw new RuntimeException("Instructor cannot purchase their own course");
        }

        User user = userRepository.findById(studentId).orElseThrow();
        if (user.getRole() == com.edu.dsalearningplatform.enums.UserRole.ADMIN) {
            throw new RuntimeException("Admin cannot purchase courses");
        }

        return new PurchaseContext(course, user);
    }

    private void ensureVnpayConfigured() {
        if (vnpTmnCode == null || vnpTmnCode.isBlank() || vnpHashSecret == null || vnpHashSecret.isBlank()) {
            throw new IllegalStateException("VNPAY sandbox is not configured. Please set VNPAY_TMN_CODE and VNPAY_HASH_SECRET.");
        }
    }

    private String sanitizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "127.0.0.1";
        }
        return ipAddress;
    }

    private String generateTxnRef(Integer studentId, Long courseId) {
        return "DSA_" + courseId + "_" + studentId + "_" + System.currentTimeMillis();
    }

    private ParsedTxn parseTxnRef(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }

        String[] parts = txnRef.split("_");
        if (parts.length < 4 || !"DSA".equals(parts[0])) {
            return null;
        }

        try {
            Long courseId = Long.parseLong(parts[1]);
            Integer studentId = Integer.parseInt(parts[2]);
            return new ParsedTxn(courseId, studentId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal parseGatewayAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(rawAmount).divide(BigDecimal.valueOf(100));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                builder.append('&');
            }
            builder.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
            first = false;
        }
        return builder.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot create VNPAY secure hash", ex);
        }
    }

    private record PurchaseContext(Course course, User user) {
    }

    private record ParsedTxn(Long courseId, Integer studentId) {
    }

    @Override
    public List<InstructorPaymentDTO> getPaymentsByInstructor(Integer instructorId) {
        return paymentRepository.findByCourse_Instructor_UserId(instructorId);
    }

    @Override
    public List<InstructorPaymentDTO> getAllPayments() {
        return paymentRepository.findAllPaymentsForAdmin();
    }

    @Override
    public List<Object[]> getBestSellingCoursesThisMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        return paymentRepository.findBestSellingCourses(startOfMonth, endOfMonth);
    }

    @Override
    public List<Object[]> getRevenueLast3Months() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOf3MonthsAgo = now.minusMonths(2).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return paymentRepository.findMonthlyRevenue(startOf3MonthsAgo);
    }
}
