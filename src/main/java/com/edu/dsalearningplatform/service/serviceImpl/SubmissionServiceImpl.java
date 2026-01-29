package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.Submission;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.SubmissionRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.AiService;
import com.edu.dsalearningplatform.service.SubmissionService;
import org.springframework.stereotype.Service;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository, CourseRepository courseRepository, UserRepository userRepository, AiService aiService) {
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @Override
    public Submission submitCode(Long courseId, Integer studentId, String code) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        User user = userRepository.findById(studentId).orElseThrow();
        Submission s = new Submission();
        s.setCourse(course);
        s.setStudent(user);
        s.setCode(code);
        // Use AI service to evaluate code
        int score = aiService.evaluateCode(code);
        s.setScore(score);
        return submissionRepository.save(s);
    }
}
