package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public int evaluateCode(String code) {
        if (code == null || code.isBlank()) {
            return 0;
        }
        try {
            String out = chatClient.prompt()
                    .system("Bạn là reviewer code. Chỉ trả về 1 số nguyên từ 0 đến 100.")
                    .user("Chấm điểm đoạn code sau:\n" + code)
                    .call()
                    .content();

            String digits = out.replaceAll("[^0-9]", "");
            if (digits.isBlank()) {
                return 70;
            }
            int score = Integer.parseInt(digits);
            return Math.max(0, Math.min(100, score));
        } catch (Exception e) {
            return 70;
        }
    }

    @Override
    public String recommendNextTopic(Integer studentId, Long courseId) {
        try {
            return chatClient.prompt()
                    .system("Bạn là mentor DSA, gợi ý ngắn gọn 1-2 chủ đề học tiếp theo.")
                    .user("Học viên " + studentId + " trong khóa " + courseId + " nên học gì tiếp theo?")
                    .call()
                    .content();
        } catch (Exception e) {
            return "Bạn nên ôn Big-O, sau đó học Sorting và Binary Search.";
        }
    }
}
