package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.ProgressEvent;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.ProgressEventRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.AiService;
import com.edu.dsalearningplatform.service.ProgressService;
import org.springframework.stereotype.Service;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final ProgressEventRepository progressEventRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AiService aiService;

    public ProgressServiceImpl(ProgressEventRepository progressEventRepository, UserRepository userRepository, CourseRepository courseRepository, AiService aiService) {
        this.progressEventRepository = progressEventRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.aiService = aiService;
    }

    @Override
    public ProgressEvent recordProgress(Integer studentId, Long courseId, String eventType, Integer value) {
        User u = userRepository.findById(studentId).orElseThrow();
        Course c = courseRepository.findById(courseId).orElseThrow();
        ProgressEvent ev = new ProgressEvent();
        ev.setStudent(u);
        ev.setCourse(c);
        ev.setEventType(eventType);
        ev.setValue(value);
        return progressEventRepository.save(ev);
    }

    @Override
    public String recommendNextTopic(Integer studentId, Long courseId) {
        return aiService.recommendNextTopic(studentId, courseId);
    }
}
