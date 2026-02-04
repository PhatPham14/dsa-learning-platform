package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.dto.request.CreateCourseRequest;
import com.edu.dsalearningplatform.entity.*;
import com.edu.dsalearningplatform.repository.*;
import com.edu.dsalearningplatform.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final VideoRepository videoRepository;
    private final QuizRepository quizRepository;
    private final AssignmentRepository assignmentRepository;

    public CourseServiceImpl(CourseRepository courseRepository, UserRepository userRepository,
                             SessionRepository sessionRepository, VideoRepository videoRepository,
                             QuizRepository quizRepository, AssignmentRepository assignmentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.videoRepository = videoRepository;
        this.quizRepository = quizRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public Course createCourse(CreateCourseRequest req) {
        Course course = new Course();
        course.setTitle(req.title);
        course.setDescription(req.description);
        course.setPrice(req.price);
        course.setImageBase64(req.imageBase64);
        if (req.instructorId != null) {
            User instructor = userRepository.findById(req.instructorId).orElse(null);
            course.setInstructor(instructor);
        }
        course.setActive(false); // Default to inactive, waiting for admin approval
        course.setPublished(false);
        return courseRepository.save(course);
    }

    @Override
    public List<Course> getFeaturedCourses() {
        return courseRepository.findTop6ByIsPublishedTrueAndIsActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<Course> getPendingCourses() {
        return courseRepository.findByIsActiveFalse();
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> getCoursesByInstructor(Integer instructorId) {
        return courseRepository.findAll().stream()
                .filter(c -> c.getInstructor() != null && c.getInstructor().getUserId().equals(instructorId))
                .collect(Collectors.toList());
    }

    @Override
    public Course getCourseById(Long courseId) {
        if (courseId == null) {
            return null;
        }
        return courseRepository.findById(courseId).orElse(null);
    }

    @Override
    public void toggleStatus(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        course.setActive(!course.isActive());
        courseRepository.save(course);
    }

    // Session management
    @Override
    public Session addSession(Long courseId, Session session) {
        Course course = getCourseById(courseId);
        if (course == null) throw new RuntimeException("Course not found");
        session.setCourse(course);
        return sessionRepository.save(session);
    }

    @Override
    public Session updateSession(Long sessionId, Session sessionDetails) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        session.setTitle(sessionDetails.getTitle());
        session.setDescription(sessionDetails.getDescription());
        session.setSessionOrder(sessionDetails.getSessionOrder());
        return sessionRepository.save(session);
    }

    @Override
    public void deleteSession(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    @Override
    public Session getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    // Video management
    @Override
    public Video addVideo(Long sessionId, Video video) {
        Session session = getSessionById(sessionId);
        if (session == null) throw new RuntimeException("Session not found");
        video.setSession(session);
        return videoRepository.save(video);
    }

    @Override
    public Video updateVideo(Long videoId, Video videoDetails) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
        video.setTitle(videoDetails.getTitle());
        video.setVideoUrl(videoDetails.getVideoUrl());
        video.setDuration(videoDetails.getDuration());
        return videoRepository.save(video);
    }

    @Override
    public void deleteVideo(Long videoId) {
        videoRepository.deleteById(videoId);
    }

    @Override
    public Video getVideoById(Long videoId) {
        return videoRepository.findById(videoId).orElse(null);
    }

    // Quiz management
    @Override
    public Quiz addQuiz(Long sessionId, Quiz quiz) {
        Session session = getSessionById(sessionId);
        if (session == null) throw new RuntimeException("Session not found");
        quiz.setSession(session);
        return quizRepository.save(quiz);
    }

    @Override
    public Quiz updateQuiz(Long quizId, Quiz quizDetails) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));
        quiz.setTitle(quizDetails.getTitle());
        quiz.setDescription(quizDetails.getDescription());
        return quizRepository.save(quiz);
    }

    @Override
    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }

    @Override
    public Quiz getQuizById(Long quizId) {
        return quizRepository.findById(quizId).orElse(null);
    }

    // Assignment management
    @Override
    public Assignment addAssignment(Long sessionId, Assignment assignment) {
        Session session = getSessionById(sessionId);
        if (session == null) throw new RuntimeException("Session not found");
        assignment.setSession(session);
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Long assignmentId, Assignment assignmentDetails) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new RuntimeException("Assignment not found"));
        assignment.setTitle(assignmentDetails.getTitle());
        assignment.setDescription(assignmentDetails.getDescription());
        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    @Override
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId).orElse(null);
    }
}
