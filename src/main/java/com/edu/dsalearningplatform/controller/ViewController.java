package com.edu.dsalearningplatform.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ViewController {

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
        }
        // Lấy danh sách khóa học từ service
        List<com.edu.dsalearningplatform.entity.Course> courses = courseService.getAllCourses();

        // Nếu không phải Admin, chỉ hiển thị khóa học đang active
        if (user == null || user.getRole() != UserRole.ADMIN) {
            courses = courses.stream()
                    .filter(com.edu.dsalearningplatform.entity.Course::isActive)
                    .collect(Collectors.toList());
        }

        model.addAttribute("courses", courses);
        return "courses";
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
    public String adminDashboard(HttpServletRequest request, Model model) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return "redirect:/login";
            }
            if (user.getRole() != UserRole.ADMIN) {
                return "redirect:/";
            }
            model.addAttribute("user", user);

            var payments = paymentService.getAllPayments();
            model.addAttribute("payments", payments);

            BigDecimal totalRevenue = payments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            BigDecimal totalAdminShare = payments.stream()
                .map(p -> p.getAdminShare() != null ? p.getAdminShare() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalAdminShare", totalAdminShare);

            // Aggregate revenue by course
            Map<String, BigDecimal> revenueByCourse = payments.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getCourseTitle() != null ? p.getCourseTitle() : "Unknown Course",
                    Collectors.reducing(BigDecimal.ZERO, 
                        p -> p.getAdminShare() != null ? p.getAdminShare() : BigDecimal.ZERO, 
                        BigDecimal::add)
                ));
            model.addAttribute("revenueByCourse", revenueByCourse);

            // New stats
            List<Object[]> bestSellingCourses = paymentService.getBestSellingCoursesThisMonth();
            model.addAttribute("bestSellingCourses", bestSellingCourses);

            List<Object[]> revenueLast3Months = paymentService.getRevenueLast3Months();
            model.addAttribute("revenueLast3Months", revenueLast3Months);

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
    public String instructorDashboard(HttpServletRequest request, Model model) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return "redirect:/login";
            }
            if (user.getRole() != UserRole.INSTRUCTOR) {
                return "redirect:/";
            }
            model.addAttribute("user", user);

            var payments = paymentService.getPaymentsByInstructor(user.getUserId());
            model.addAttribute("payments", payments);

            BigDecimal totalRevenue = payments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            BigDecimal totalInstructorShare = payments.stream()
                .map(p -> p.getInstructorShare() != null ? p.getInstructorShare() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalInstructorShare", totalInstructorShare);

            // Aggregate revenue by course
            Map<String, BigDecimal> revenueByCourse = payments.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getCourseTitle() != null ? p.getCourseTitle() : "Unknown Course",
                    Collectors.reducing(BigDecimal.ZERO, 
                        p -> p.getInstructorShare() != null ? p.getInstructorShare() : BigDecimal.ZERO, 
                        BigDecimal::add)
                ));
            model.addAttribute("revenueByCourse", revenueByCourse);

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

    @GetMapping("/language")
    public String changeLanguage(@RequestParam(name = "lang") String lang,
                                 HttpServletRequest request) {
        // LocaleChangeInterceptor sẽ xử lý thay đổi locale và set cookie dựa trên tham số lang
        // Ở bước redirect, KHÔNG cần giữ lại query lang nữa để tránh bị cộng dồn trên URL
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                String path = uri.getPath();
                if (path == null || path.isEmpty()) {
                    path = "/";
                }
                // Redirect về lại đúng path ban đầu, KHÔNG kèm query
                return "redirect:" + path;
            } catch (Exception e) {
                // Nếu parse lỗi, quay về trang chủ
                return "redirect:/";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/language")
    public String changeLanguagePost(@RequestParam(name = "lang") String lang, HttpServletRequest request) {
        // Delegate to the same logic as GET /language so UI can POST with CSRF token
        return changeLanguage(lang, request);
    }
}
