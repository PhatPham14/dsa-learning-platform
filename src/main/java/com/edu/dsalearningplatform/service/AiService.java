package com.edu.dsalearningplatform.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AiService {

    private final Random rnd = new Random();

    // Returns a simulated score (0-100) for submitted code
    public int evaluateCode(String code) {
        if (code == null || code.isBlank()) return 0;
        // simple heuristic: longer code -> a bit higher score, random factor
        int base = Math.min(80, Math.max(10, code.length() / 10));
        return Math.min(100, base + rnd.nextInt(21));
    }

    // Returns a simple personalized recommendation string
    public String recommendNextTopic(Integer studentId, Long courseId) {
        // stubbed recommendations
        String[] recs = new String[]{
                "Practice more on recursion problems",
                "Review array and string algorithms",
                "Work on graph traversal exercises",
                "Focus on time complexity optimizations",
                "Try more dynamic programming challenges"
        };
        return recs[(studentId == null ? 0 : Math.abs(studentId.hashCode())) % recs.length];
    }
}
