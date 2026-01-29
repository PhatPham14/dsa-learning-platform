package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.security.jwt.JwtUtils;
import com.edu.dsalearningplatform.service.CourseService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @Autowired(required = false)
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    private final CourseService courseService;

    public ViewController(CourseService courseService) {
        this.courseService = courseService;
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
            return userRepository.findByPhone(username)
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
        return "home";
    }

    @GetMapping("/courses")
    public String courses(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user != null) {
            model.addAttribute("user", user);
        }
        // Lấy danh sách khóa học từ service để render trực tiếp bằng Thymeleaf
        model.addAttribute("courses", courseService.getAllCourses());
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
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/submit")
    public String submit(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "submit";
    }

    @GetMapping("/create")
    public String create(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "createCourse";
    }

    @GetMapping("/purchase")
    public String purchase(@RequestParam(name = "courseId", required = false) Long courseId,
                           HttpServletRequest request,
                           Model model) {
        User user = getCurrentUser(request);
        if (user != null) {
            model.addAttribute("user", user);
        }

        // Load thông tin khóa học từ database
        if (courseId != null) {
            var course = courseService.getCourseById(courseId);
            model.addAttribute("course", course);
        }
        model.addAttribute("courseId", courseId);
        return "purchase";
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
        model.addAttribute("user", user);
        // TODO: Khi có logic enrollment, truyền danh sách khóa học đã mua vào model
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
