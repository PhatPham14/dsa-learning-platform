package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.dto.request.CreateCourseRequest;
import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseServiceImpl(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Course createCourse(CreateCourseRequest req) {
        Course course = new Course();
        course.setTitle(req.title);
        course.setDescription(req.description);
        course.setPrice(req.price);
        if (req.instructorId != null) {
            User instructor = userRepository.findById(req.instructorId).orElse(null);
            course.setInstructor(instructor);
        }
        return courseRepository.save(course);
    }

    @Override
    public List<Course> getFeaturedCourses() {
        return courseRepository.findTop6ByIsPublishedTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<Course> getAllCourses() {
        // Hiện tại lấy tất cả; sau này có thể đổi thành chỉ lấy isPublished = true
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(Long courseId) {
        if (courseId == null) {
            return null;
        }
        return courseRepository.findById(courseId).orElse(null);
    }
}
