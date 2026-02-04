package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.entity.*;
import com.edu.dsalearningplatform.enums.UserRole;
import com.edu.dsalearningplatform.security.jwt.JwtUtils;
import com.edu.dsalearningplatform.service.CourseService;
import com.edu.dsalearningplatform.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    @Autowired(required = false)
    private JwtUtils jwtUtils;

    private final CourseService courseService;
    private final UserService userService;

    public InstructorController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

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
            return userService.getUserByPhone(username)
                    .orElseGet(() -> {
                        User tempUser = new User();
                        tempUser.setEmail(username);
                        tempUser.setFullName(username);
                        tempUser.setRole(UserRole.STUDENT);
                        return tempUser;
                    });
        }
        return null;
    }

    private boolean isInstructor(User user) {
        return user != null && user.getRole() == UserRole.INSTRUCTOR;
    }

    @GetMapping("/courses")
    public String listCourses(HttpServletRequest request, Model model) {
        User user = getCurrentUser(request);
        if (user == null) return "redirect:/login";
        if (!isInstructor(user)) return "redirect:/";

        List<Course> courses = courseService.getCoursesByInstructor(user.getUserId());
        model.addAttribute("courses", courses);
        model.addAttribute("user", user);
        return "instructor/courses";
    }

    @GetMapping("/courses/{courseId}/edit")
    public String editCourse(@PathVariable Long courseId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null) return "redirect:/login";
        if (!isInstructor(user)) return "redirect:/";

        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "Course not found");
            return "redirect:/instructor/courses";
        }

        if (course.getInstructor() == null || !course.getInstructor().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this course");
            return "redirect:/instructor/courses";
        }

        // Initialize lazy collections
        if (course.getSessions() != null) {
            course.getSessions().size();
            for (Session s : course.getSessions()) {
                if (s.getVideos() != null) s.getVideos().size();
                if (s.getQuizzes() != null) s.getQuizzes().size();
                if (s.getAssignments() != null) s.getAssignments().size();
            }
        }

        model.addAttribute("course", course);
        model.addAttribute("user", user);
        return "instructor/course-editor";
    }

    // --- Session Management ---

    @GetMapping("/sessions/{sessionId}/edit")
    public String editSession(@PathVariable Long sessionId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Session session = courseService.getSessionById(sessionId);
        if (session == null || !session.getCourse().getInstructor().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized or session not found");
            return "redirect:/instructor/courses";
        }
        
        model.addAttribute("item", session);
        model.addAttribute("itemType", "session");
        model.addAttribute("courseId", session.getCourse().getCourseId());
        return "instructor/edit-item";
    }

    @PostMapping("/sessions/{sessionId}/edit")
    public String updateSession(@PathVariable Long sessionId, @ModelAttribute Session sessionDetails, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";
        
        Session session = courseService.getSessionById(sessionId);
        if (session == null || !session.getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             return "redirect:/instructor/courses";
        }

        session.setTitle(sessionDetails.getTitle());
        session.setDescription(sessionDetails.getDescription());
        session.setSessionOrder(sessionDetails.getSessionOrder());
        courseService.updateSession(sessionId, session);
        
        redirectAttributes.addFlashAttribute("success", "Session updated successfully");
        return "redirect:/instructor/courses/" + session.getCourse().getCourseId() + "/edit";
    }

    @PostMapping("/courses/{courseId}/sessions")
    public String addSession(@PathVariable Long courseId, @ModelAttribute Session session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        // Verify ownership
        Course course = courseService.getCourseById(courseId);
        if (course == null || course.getInstructor() == null || !course.getInstructor().getUserId().equals(user.getUserId())) {
             return "redirect:/instructor/courses";
        }

        courseService.addSession(courseId, session);
        redirectAttributes.addFlashAttribute("success", "Session added successfully");
        return "redirect:/instructor/courses/" + courseId + "/edit";
    }

    @PostMapping("/sessions/{sessionId}/delete")
    public String deleteSession(@PathVariable Long sessionId, @RequestParam Long courseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        // Ideally check ownership of session via course, skipping for brevity but relying on courseId passed or lookup
        // Robust way: get session -> get course -> check instructor
        Session session = courseService.getSessionById(sessionId);
        if (session != null && session.getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             courseService.deleteSession(sessionId);
             redirectAttributes.addFlashAttribute("success", "Session deleted successfully");
        } else {
             redirectAttributes.addFlashAttribute("error", "Unauthorized or session not found");
        }
        
        return "redirect:/instructor/courses/" + courseId + "/edit";
    }

    // --- Video Management ---

    @GetMapping("/videos/{videoId}/edit")
    public String editVideo(@PathVariable Long videoId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Video video = courseService.getVideoById(videoId);
        if (video == null || !video.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             redirectAttributes.addFlashAttribute("error", "Unauthorized or video not found");
             return "redirect:/instructor/courses";
        }
        
        model.addAttribute("item", video);
        model.addAttribute("itemType", "video");
        model.addAttribute("courseId", video.getSession().getCourse().getCourseId());
        return "instructor/edit-item";
    }

    @PostMapping("/videos/{videoId}/edit")
    public String updateVideo(@PathVariable Long videoId, @ModelAttribute Video videoDetails, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Video video = courseService.getVideoById(videoId);
        if (video == null || !video.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             return "redirect:/instructor/courses";
        }

        video.setTitle(videoDetails.getTitle());
        video.setVideoUrl(videoDetails.getVideoUrl());
        video.setDuration(videoDetails.getDuration());
        courseService.updateVideo(videoId, video);

        redirectAttributes.addFlashAttribute("success", "Video updated successfully");
        return "redirect:/instructor/courses/" + video.getSession().getCourse().getCourseId() + "/edit";
    }

    @PostMapping("/sessions/{sessionId}/videos")
    public String addVideo(@PathVariable Long sessionId, @ModelAttribute Video video, @RequestParam Long courseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Session session = courseService.getSessionById(sessionId);
        if (session != null && session.getCourse().getInstructor().getUserId().equals(user.getUserId())) {
            courseService.addVideo(sessionId, video);
            redirectAttributes.addFlashAttribute("success", "Video added successfully");
        }
        
        return "redirect:/instructor/courses/" + courseId + "/edit";
    }

    @PostMapping("/videos/{videoId}/delete")
    public String deleteVideo(@PathVariable Long videoId, @RequestParam Long courseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
         User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Video video = courseService.getVideoById(videoId);
        if (video != null && video.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
            courseService.deleteVideo(videoId);
            redirectAttributes.addFlashAttribute("success", "Video deleted successfully");
        }
        return "redirect:/instructor/courses/" + courseId + "/edit";
    }

    // --- Quiz Management ---
    @GetMapping("/quizzes/{quizId}/edit")
    public String editQuiz(@PathVariable Long quizId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Quiz quiz = courseService.getQuizById(quizId);
        if (quiz == null || !quiz.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             redirectAttributes.addFlashAttribute("error", "Unauthorized or quiz not found");
             return "redirect:/instructor/courses";
        }
        
        model.addAttribute("item", quiz);
        model.addAttribute("itemType", "quiz");
        model.addAttribute("courseId", quiz.getSession().getCourse().getCourseId());
        return "instructor/edit-item";
    }

    @PostMapping("/quizzes/{quizId}/edit")
    public String updateQuiz(@PathVariable Long quizId, @ModelAttribute Quiz quizDetails, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Quiz quiz = courseService.getQuizById(quizId);
        if (quiz == null || !quiz.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             return "redirect:/instructor/courses";
        }

        quiz.setTitle(quizDetails.getTitle());
        courseService.updateQuiz(quizId, quiz);

        redirectAttributes.addFlashAttribute("success", "Quiz updated successfully");
        return "redirect:/instructor/courses/" + quiz.getSession().getCourse().getCourseId() + "/edit";
    }

    @PostMapping("/sessions/{sessionId}/quizzes")
    public String addQuiz(@PathVariable Long sessionId, @ModelAttribute Quiz quiz, @RequestParam Long courseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Session session = courseService.getSessionById(sessionId);
        if (session != null && session.getCourse().getInstructor().getUserId().equals(user.getUserId())) {
            courseService.addQuiz(sessionId, quiz);
            redirectAttributes.addFlashAttribute("success", "Quiz added successfully");
        }

        return "redirect:/instructor/courses/" + courseId + "/edit";
    }

    @PostMapping("/quizzes/{quizId}/delete")
    public String deleteQuiz(@PathVariable Long quizId, @RequestParam Long courseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Quiz quiz = courseService.getQuizById(quizId);
        if (quiz != null && quiz.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
            courseService.deleteQuiz(quizId);
            redirectAttributes.addFlashAttribute("success", "Quiz deleted successfully");
        }
        return "redirect:/instructor/courses/" + courseId + "/edit";
    }

    // --- Assignment Management ---
    @GetMapping("/assignments/{assignmentId}/edit")
    public String editAssignment(@PathVariable Long assignmentId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Assignment assignment = courseService.getAssignmentById(assignmentId);
        if (assignment == null || !assignment.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             redirectAttributes.addFlashAttribute("error", "Unauthorized or assignment not found");
             return "redirect:/instructor/courses";
        }
        
        model.addAttribute("item", assignment);
        model.addAttribute("itemType", "assignment");
        model.addAttribute("courseId", assignment.getSession().getCourse().getCourseId());
        return "instructor/edit-item";
    }

    @PostMapping("/assignments/{assignmentId}/edit")
    public String updateAssignment(@PathVariable Long assignmentId, @ModelAttribute Assignment assignmentDetails, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Assignment assignment = courseService.getAssignmentById(assignmentId);
        if (assignment == null || !assignment.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
             return "redirect:/instructor/courses";
        }

        assignment.setTitle(assignmentDetails.getTitle());
        assignment.setDescription(assignmentDetails.getDescription());
        courseService.updateAssignment(assignmentId, assignment);

        redirectAttributes.addFlashAttribute("success", "Assignment updated successfully");
        return "redirect:/instructor/courses/" + assignment.getSession().getCourse().getCourseId() + "/edit";
    }

    @PostMapping("/sessions/{sessionId}/assignments")
    public String addAssignment(@PathVariable Long sessionId, @ModelAttribute Assignment assignment, @RequestParam Long courseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Session session = courseService.getSessionById(sessionId);
        if (session != null && session.getCourse().getInstructor().getUserId().equals(user.getUserId())) {
            courseService.addAssignment(sessionId, assignment);
            redirectAttributes.addFlashAttribute("success", "Assignment added successfully");
        }

        return "redirect:/instructor/courses/" + courseId + "/edit";
    }

    @PostMapping("/assignments/{assignmentId}/delete")
    public String deleteAssignment(@PathVariable Long assignmentId, @RequestParam Long courseId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(request);
        if (user == null || !isInstructor(user)) return "redirect:/login";

        Assignment assignment = courseService.getAssignmentById(assignmentId);
        if (assignment != null && assignment.getSession().getCourse().getInstructor().getUserId().equals(user.getUserId())) {
            courseService.deleteAssignment(assignmentId);
            redirectAttributes.addFlashAttribute("success", "Assignment deleted successfully");
        }
        return "redirect:/instructor/courses/" + courseId + "/edit";
    }
}
