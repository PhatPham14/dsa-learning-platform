package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.dto.InstructorPaymentDTO;
import com.edu.dsalearningplatform.dto.request.UpdateProfileRequest;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.enums.UserRole;
import com.edu.dsalearningplatform.security.jwt.JwtUtils;
import com.edu.dsalearningplatform.service.CourseService;
import com.edu.dsalearningplatform.service.EnrollmentService;
import com.edu.dsalearningplatform.service.PaymentService;
import com.edu.dsalearningplatform.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    private static final int DAILY_DASHBOARD_DAYS = 10;

    @Autowired(required = false)
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;

    public ViewController(CourseService courseService, EnrollmentService enrollmentService, PaymentService paymentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Nếu không có header Authorization, thử lấy từ cookie accessToken
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (token != null && jwtUtils != null && jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUsernameFromJwtToken(token);
            // Query user thật từ database để có đầy đủ thông tin (userId, fullName, ...)
            // JWT subject trong hệ thống này là phone number
            return userService.getUserByPhone(username)
                    .orElseGet(() -> {
                        // Fallback: tạo user tạm nếu không tìm thấy trong DB
                        User tempUser = new User();
                        tempUser.setEmail(username);
                        tempUser.setFullName(username);
                        tempUser.setRole(UserRole.STUDENT);
                        return tempUser;
                    });
        }
        return null;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user != null) {
            model.addAttribute("user", user);
        }
        // Khóa học nổi bật cho trang chủ, không yêu cầu đăng nhập
        model.addAttribute("featuredCourses", courseService.getFeaturedCourses());
        return "index";
    }

    @GetMapping("/courses")
    public String courses(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user != null) {
            model.addAttribute("user", user);
            // Get enrolled courses for the current user
            var enrollments = enrollmentService.getEnrollmentsByStudent(user.getUserId());
            Set<Long> enrolledCourseIds = enrollments.stream()
                    .map(e -> e.getCourse().getCourseId())
                    .collect(Collectors.toSet());
            model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        }
        // Lấy danh sách khóa học từ service
        List<com.edu.dsalearningplatform.entity.Course> courses = courseService.getAllCourses();
        
        // DEBUG: Print image data status
        System.out.println("=== DEBUGGING IMAGE DATA ===");
        for (com.edu.dsalearningplatform.entity.Course c : courses) {
            String img = c.getImageBase64();
            String status = (img == null) ? "NULL" : "LENGTH=" + img.length();
            String prefix = (img != null && img.length() > 20) ? img.substring(0, 20) : img;
            System.out.println("Course ID: " + c.getCourseId() + 
                               ", Title: " + c.getTitle() + 
                               ", ImageData: " + status + 
                               ", Prefix: " + prefix);
        }
        System.out.println("============================");

        // Nếu không phải Admin, chỉ hiển thị khóa học đang active
        if (user == null || user.getRole() != UserRole.ADMIN) {
            courses = courses.stream()
                    .filter(com.edu.dsalearningplatform.entity.Course::isActive)
                    .collect(Collectors.toList());
        }

        model.addAttribute("courses", courses);
        return "courses";
    }

    @GetMapping("/courses/{courseId}")
    public String courseDetail(@PathVariable Long courseId,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user != null) {
            model.addAttribute("user", user);
        }

        var course = courseService.getCourseById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy khóa học.");
            return "redirect:/courses";
        }

        boolean isAdmin = user != null && user.getRole() == UserRole.ADMIN;
        boolean isInstructorOwner = user != null &&
                course.getInstructor() != null &&
                course.getInstructor().getUserId().equals(user.getUserId());

        // Hide inactive courses from normal users
        if (!course.isActive() && !isAdmin && !isInstructorOwner) {
            redirectAttributes.addFlashAttribute("message", "Khóa học này hiện không khả dụng.");
            return "redirect:/courses";
        }

        boolean isEnrolled = user != null && enrollmentService.isEnrolled(user.getUserId(), courseId);

        model.addAttribute("course", course);
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("isInstructorOwner", isInstructorOwner);
        model.addAttribute("isAdmin", isAdmin);

        return "courseDetail";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // POST /login hiện được xử lý trên FE bằng fetch tới /api/auth/login,
    // nên tạm thời không dùng endpoint này để tránh hiểu nhầm flow đăng nhập.

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // POST /register hiện được xử lý trên FE bằng fetch tới /api/auth/register,
    // nên không cần endpoint này nữa.

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == UserRole.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpServletRequest request,
                                 Model model,
                                 @RequestParam(name = "period", defaultValue = "day") String period,
                                 @RequestParam(name = "month", required = false) Integer month,
                                 @RequestParam(name = "year", required = false) Integer year) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return "redirect:/login";
            }
            if (user.getRole() != UserRole.ADMIN) {
                return "redirect:/";
            }
            model.addAttribute("user", user);

            List<InstructorPaymentDTO> allPayments = paymentService.getAllPayments();

            List<Integer> availableYears = getAvailableYears(allPayments);
            int resolvedYear = resolveYear(year, availableYears);
            int resolvedMonth = resolveMonth(month);
            DashboardPeriod dashboardPeriod = resolvePeriod(period);
            DashboardRange dashboardRange = buildDashboardRange(dashboardPeriod, resolvedMonth, resolvedYear);
            List<InstructorPaymentDTO> filteredPayments = filterPaymentsByRange(allPayments, dashboardRange);

            BigDecimal totalRevenue = filteredPayments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            BigDecimal totalAdminShare = filteredPayments.stream()
                .map(p -> p.getAdminShare() != null ? p.getAdminShare() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalAdminShare", totalAdminShare);
            model.addAttribute("payments", filteredPayments);

            // Aggregate revenue by course
            Map<String, BigDecimal> revenueByCourse = filteredPayments.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getCourseTitle() != null ? p.getCourseTitle() : "Unknown Course",
                    LinkedHashMap::new,
                    Collectors.reducing(BigDecimal.ZERO, 
                        p -> p.getAdminShare() != null ? p.getAdminShare() : BigDecimal.ZERO, 
                        BigDecimal::add)
                ));
            model.addAttribute("revenueByCourse", revenueByCourse);

            List<Object[]> bestSellingCourses = buildBestSellingCourses(filteredPayments);
            model.addAttribute("bestSellingCourses", bestSellingCourses);

            List<Object[]> revenueTimeline = buildRevenueTimeline(filteredPayments, dashboardPeriod, dashboardRange,
                    p -> p.getAdminShare() != null ? p.getAdminShare() : BigDecimal.ZERO);
            model.addAttribute("revenueTimeline", revenueTimeline);

            model.addAttribute("period", dashboardPeriod.name().toLowerCase());
            model.addAttribute("selectedMonth", resolvedMonth);
            model.addAttribute("selectedYear", resolvedYear);
            model.addAttribute("availableYears", availableYears);
            model.addAttribute("monthOptions", buildMonthOptions());
            model.addAttribute("periodLabel", buildPeriodLabel(dashboardPeriod, resolvedMonth, resolvedYear));
            model.addAttribute("transactionLabel", buildTransactionLabel(dashboardPeriod, resolvedMonth, resolvedYear));

            return "adminDashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/submit")
    public String submit(@RequestParam(required = false) Long courseId, HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("courseId", courseId);
        return "submit";
    }

    @GetMapping("/create")
    public String create(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        // Chỉ cho phép Instructor tạo khóa học
        if (user.getRole() != UserRole.INSTRUCTOR) {
             return "redirect:/";
        }
        model.addAttribute("user", user);
        return "createCourse";
    }

    @GetMapping("/instructor/dashboard")
    public String instructorDashboard(HttpServletRequest request,
                                      Model model,
                                      @RequestParam(name = "period", defaultValue = "day") String period,
                                      @RequestParam(name = "month", required = false) Integer month,
                                      @RequestParam(name = "year", required = false) Integer year) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return "redirect:/login";
            }
            if (user.getRole() != UserRole.INSTRUCTOR) {
                return "redirect:/";
            }
            model.addAttribute("user", user);

            List<InstructorPaymentDTO> allPayments = paymentService.getPaymentsByInstructor(user.getUserId());
            List<Integer> availableYears = getAvailableYears(allPayments);
            int resolvedYear = resolveYear(year, availableYears);
            int resolvedMonth = resolveMonth(month);
            DashboardPeriod dashboardPeriod = resolvePeriod(period);
            DashboardRange dashboardRange = buildDashboardRange(dashboardPeriod, resolvedMonth, resolvedYear);
            List<InstructorPaymentDTO> filteredPayments = filterPaymentsByRange(allPayments, dashboardRange);

            var recentPayments = filteredPayments.stream()
                .sorted(Comparator.comparing(InstructorPaymentDTO::getPaidAt,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .toList();

            model.addAttribute("payments", recentPayments);
            model.addAttribute("totalPaymentCount", filteredPayments.size());

            BigDecimal totalRevenue = filteredPayments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            BigDecimal totalInstructorShare = filteredPayments.stream()
                .map(p -> p.getInstructorShare() != null ? p.getInstructorShare() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalInstructorShare", totalInstructorShare);

            // Aggregate revenue by course
            Map<String, BigDecimal> revenueByCourse = filteredPayments.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getCourseTitle() != null ? p.getCourseTitle() : "Unknown Course",
                    LinkedHashMap::new,
                    Collectors.reducing(BigDecimal.ZERO, 
                        p -> p.getInstructorShare() != null ? p.getInstructorShare() : BigDecimal.ZERO, 
                        BigDecimal::add)
                ));
            model.addAttribute("revenueByCourse", revenueByCourse);

            List<Object[]> bestSellingCourses = buildBestSellingCourses(filteredPayments);
            model.addAttribute("bestSellingCourses", bestSellingCourses);

            List<Object[]> revenueTimeline = buildRevenueTimeline(filteredPayments, dashboardPeriod, dashboardRange,
                    p -> p.getInstructorShare() != null ? p.getInstructorShare() : BigDecimal.ZERO);
            model.addAttribute("revenueTimeline", revenueTimeline);

            model.addAttribute("period", dashboardPeriod.name().toLowerCase());
            model.addAttribute("selectedMonth", resolvedMonth);
            model.addAttribute("selectedYear", resolvedYear);
            model.addAttribute("availableYears", availableYears);
            model.addAttribute("monthOptions", buildMonthOptions());
            model.addAttribute("periodLabel", buildPeriodLabel(dashboardPeriod, resolvedMonth, resolvedYear));
            model.addAttribute("transactionLabel", buildTransactionLabel(dashboardPeriod, resolvedMonth, resolvedYear));

            return "instructorDashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/purchase")
    public String purchase(@RequestParam(name = "courseId", required = false) Long courseId,
                           HttpServletRequest request,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user != null) {
            model.addAttribute("user", user);
            if (courseId != null && enrollmentService.isEnrolled(user.getUserId(), courseId)) {
                redirectAttributes.addFlashAttribute("message", "Bạn đã sở hữu khóa học này.");
                return "redirect:/my-courses";
            }
        }

        // Load thông tin khóa học từ database
        if (courseId != null) {
            var course = courseService.getCourseById(courseId);
            
            if (course == null || !course.isActive()) {
                redirectAttributes.addFlashAttribute("message", "Khóa học này hiện không khả dụng.");
                return "redirect:/courses";
            }

            // Check if user is instructor
            if (user != null && course.getInstructor() != null && course.getInstructor().getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("message", "Bạn không thể mua khóa học do chính mình tạo.");
                return "redirect:/courses";
            }
            // Check if user is Admin
            if (user != null && user.getRole() == UserRole.ADMIN) {
                redirectAttributes.addFlashAttribute("message", "Admin không thể mua khóa học.");
                return "redirect:/courses";
            }
            model.addAttribute("course", course);
        }
        model.addAttribute("courseId", courseId);
        return "purchase";
    }

    private DashboardPeriod resolvePeriod(String period) {
        if (period == null) {
            return DashboardPeriod.DAY;
        }
        return switch (period.toLowerCase()) {
            case "month" -> DashboardPeriod.MONTH;
            case "year" -> DashboardPeriod.YEAR;
            default -> DashboardPeriod.DAY;
        };
    }

    private int resolveMonth(Integer month) {
        int currentMonth = LocalDate.now().getMonthValue();
        if (month == null || month < 1 || month > 12) {
            return currentMonth;
        }
        return month;
    }

    private int resolveYear(Integer year, List<Integer> availableYears) {
        int currentYear = LocalDate.now().getYear();
        if (year != null) {
            return year;
        }
        if (!availableYears.isEmpty()) {
            return availableYears.get(0);
        }
        return currentYear;
    }

    private DashboardRange buildDashboardRange(DashboardPeriod period, int month, int year) {
        return switch (period) {
            case DAY -> {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(DAILY_DASHBOARD_DAYS - 1L);
                yield new DashboardRange(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), month, year);
            }
            case MONTH -> {
                YearMonth yearMonth = YearMonth.of(year, month);
                yield new DashboardRange(yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59), month, year);
            }
            case YEAR -> yield new DashboardRange(LocalDate.of(year, 1, 1).atStartOfDay(),
                    LocalDate.of(year, 12, 31).atTime(23, 59, 59), month, year);
        };
    }

    private List<InstructorPaymentDTO> filterPaymentsByRange(List<InstructorPaymentDTO> payments, DashboardRange range) {
        return payments.stream()
                .filter(p -> p.getPaidAt() != null)
                .filter(p -> !p.getPaidAt().isBefore(range.start()) && !p.getPaidAt().isAfter(range.end()))
                .toList();
    }

    private List<Integer> getAvailableYears(List<InstructorPaymentDTO> payments) {
        TreeSet<Integer> years = payments.stream()
                .map(InstructorPaymentDTO::getPaidAt)
                .filter(java.util.Objects::nonNull)
                .map(LocalDateTime::getYear)
                .collect(Collectors.toCollection(TreeSet::new));
        List<Integer> availableYears = new ArrayList<>(years.descendingSet());
        if (availableYears.isEmpty()) {
            availableYears.add(LocalDate.now().getYear());
        }
        return availableYears;
    }

    private List<Integer> buildMonthOptions() {
        List<Integer> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            months.add(month);
        }
        return months;
    }

    private List<Object[]> buildBestSellingCourses(List<InstructorPaymentDTO> payments) {
        return payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCourseTitle() != null ? p.getCourseTitle() : "Unknown Course",
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .toList();
    }

    private List<Object[]> buildRevenueTimeline(List<InstructorPaymentDTO> payments,
                                                DashboardPeriod period,
                                                DashboardRange range,
                                                Function<InstructorPaymentDTO, BigDecimal> revenueExtractor) {
        LinkedHashMap<String, BigDecimal> timeline = new LinkedHashMap<>();

        if (period == DashboardPeriod.YEAR) {
            for (int month = 1; month <= 12; month++) {
                String label = String.format("%02d/%d", month, range.year());
                timeline.put(label, BigDecimal.ZERO);
            }
        } else {
            LocalDate currentDate = range.start().toLocalDate();
            LocalDate endDate = range.end().toLocalDate();
            while (!currentDate.isAfter(endDate)) {
                timeline.put(currentDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")), BigDecimal.ZERO);
                currentDate = currentDate.plusDays(1);
            }
        }

        for (InstructorPaymentDTO payment : payments) {
            String label;
            if (period == DashboardPeriod.YEAR) {
                label = String.format("%02d/%d", payment.getPaidAt().getMonthValue(), payment.getPaidAt().getYear());
            } else {
                label = payment.getPaidAt().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            }
            BigDecimal amount = revenueExtractor.apply(payment);
            timeline.merge(label, amount != null ? amount : BigDecimal.ZERO, BigDecimal::add);
        }

        return timeline.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .toList();
    }

    private String buildPeriodLabel(DashboardPeriod period, int month, int year) {
        return switch (period) {
            case DAY -> "10 ngày gần nhất";
            case MONTH -> String.format("Tháng %02d/%d", month, year);
            case YEAR -> "Năm " + year;
        };
    }

    private String buildTransactionLabel(DashboardPeriod period, int month, int year) {
        return switch (period) {
            case DAY -> "Giao dịch trong 10 ngày gần nhất";
            case MONTH -> String.format("Giao dịch tháng %02d/%d", month, year);
            case YEAR -> "Giao dịch năm " + year;
        };
    }

    private enum DashboardPeriod {
        DAY,
        MONTH,
        YEAR
    }

    private record DashboardRange(LocalDateTime start, LocalDateTime end, int month, int year) {
    }

    @PostMapping("/admin/course/approve")
    public String approveCourse(@RequestParam Long courseId, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }
        // Assuming toggleStatus sets it to active if it was inactive
        // Or we can check status before toggling, but toggle is fine for now as it's binary
        courseService.toggleStatus(courseId);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/course/toggle-status")
    public String toggleCourseStatus(@RequestParam Long courseId, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null || user.getRole() != UserRole.ADMIN) {
            return "redirect:/login";
        }
        courseService.toggleStatus(courseId);
        return "redirect:/courses";
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpServletRequest request,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam(required = false) String dateOfBirth,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String address,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
            updateProfileRequest.setFullName(fullName);
            updateProfileRequest.setEmail(email);
            updateProfileRequest.setPhone(phone);
            updateProfileRequest.setDateOfBirth(dateOfBirth);
            updateProfileRequest.setGender(gender);
            updateProfileRequest.setAddress(address);

            userService.updateMyProfile(user, updateProfileRequest);
            redirectAttributes.addFlashAttribute("message", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật hồ sơ thất bại: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @GetMapping("/my-courses")
    public String myCourses(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() == UserRole.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("user", user);
        
        var enrollments = enrollmentService.getEnrollmentsByStudent(user.getUserId());
        model.addAttribute("enrollments", enrollments);
        
        return "myCourses";
    }

    @GetMapping("/learn")
    public String learn(@RequestParam Long courseId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        
        // Check enrollment
        if (!enrollmentService.isEnrolled(user.getUserId(), courseId)) {
            // Also allow instructor of the course
            var course = courseService.getCourseById(courseId);
            if (course != null && course.getInstructor().getUserId().equals(user.getUserId())) {
                // Instructor can view their own course
            } else {
                redirectAttributes.addFlashAttribute("message", "Bạn chưa đăng ký khóa học này.");
                return "redirect:/courses";
            }
        }

        var course = courseService.getCourseById(courseId);
        if (course == null) {
             return "redirect:/courses";
        }

        // Force initialize sessions and videos to prevent LazyInitializationException in view
        if (course.getSessions() != null) {
            course.getSessions().size(); // Initialize sessions
            for (var session : course.getSessions()) {
                if (session.getVideos() != null) session.getVideos().size(); // Initialize videos
                if (session.getQuizzes() != null) session.getQuizzes().size(); // Initialize quizzes
                if (session.getAssignments() != null) session.getAssignments().size(); // Initialize assignments
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("course", course);
        return "learning";
    }

    @GetMapping("/admin/users")
    public String adminUsers(HttpServletRequest request, Model model) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return "redirect:/login";
            }
            if (user.getRole() != UserRole.ADMIN) {
                return "redirect:/";
            }
            model.addAttribute("user", user);
            
            // Get all users with default pagination and no filters
            var users = userService.getAllUsers(null, null, null, null, null, 0, 1000, "userId,desc");
            model.addAttribute("users", users);
            long studentCount = users.stream().filter(u -> "STUDENT".equalsIgnoreCase(u.getRole())).count();
            long instructorCount = users.stream().filter(u -> "INSTRUCTOR".equalsIgnoreCase(u.getRole())).count();
            long activeUserCount = users.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count();
            model.addAttribute("studentCount", studentCount);
            model.addAttribute("instructorCount", instructorCount);
            model.addAttribute("activeUserCount", activeUserCount);
            
            return "admin/users";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/admin/courses")
    public String adminCourses(HttpServletRequest request, Model model) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return "redirect:/login";
            }
            if (user.getRole() != UserRole.ADMIN) {
                return "redirect:/";
            }
            model.addAttribute("user", user);
            
            // Get all courses including inactive ones
            var courses = courseService.getAllCourses();
            model.addAttribute("courses", courses);
                long activeCourseCount = courses.stream().filter(com.edu.dsalearningplatform.entity.Course::isActive).count();
                long inactiveCourseCount = courses.size() - activeCourseCount;
                int totalEnrollments = 0;
                model.addAttribute("activeCourseCount", activeCourseCount);
                model.addAttribute("inactiveCourseCount", inactiveCourseCount);
                model.addAttribute("totalEnrollments", totalEnrollments);
            
            return "admin/courses";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading courses: " + e.getMessage());
            return "error";
        }
    }
}
