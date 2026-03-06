package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.dto.response.ChatResponse;
import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.service.AIAssistantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AIAssistantServiceImpl implements AIAssistantService {

    private final ChatClient chatClient;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIAssistantServiceImpl(ChatClient.Builder chatClientBuilder, CourseRepository courseRepository) {
        this.chatClient = chatClientBuilder.build();
        this.courseRepository = courseRepository;
    }

    @Override
    public ChatResponse searchCourses(String userMessage) {
        IntentInfo intent = analyzeIntent(userMessage);

        List<ChatResponse.CourseSearchResult> matchedCourses = List.of();
        if (intent.courseSearch()) {
            matchedCourses = findCourses(intent);
        }

        String systemPrompt = """
                Bạn là trợ lý AI thông minh chuyên về DSA (Data Structures & Algorithms).
                
                Phong cách:
                - Trò chuyện tự nhiên, gần gũi như bạn bè, không rập khuôn
                - Linh hoạt, sáng tạo trong cách diễn đạt
                - Giải thích dễ hiểu, có ví dụ thực tế khi cần
                - Khuyến khích học tập, động viên người học
                
                
                Phạm vi:
                - DSA: cấu trúc dữ liệu, giải thuật, độ phức tạp, big-O
                - Lập trình nền tảng: design patterns, best practices
                - Khóa học: CHỈ giới thiệu từ COURSE_CONTEXT được cung cấp
                
                Nếu hỏi ngoài phạm vi: từ chối nhẹ nhàng, chuyển hướng về DSA một cách thú vị.
                
                Ví dụ phong cách TỐT:
                User: "thuật toán sắp xếp nào nhanh nhất?"
                Bot: "Hỏi hay đấy! Thực ra 'nhanh nhất' còn tùy vào dữ liệu nữa bạn ạ. Nếu dữ liệu gần như đã sắp xếp rồi thì Insertion Sort bay ngon luôn với O(n). Còn trường hợp chung chung thì Quick Sort và Merge Sort đều ổn với O(n log n). Quick Sort thường nhanh hơn trong thực tế vì cache-friendly, nhưng worst case là O(n²) đấy. Bạn đang làm bài toán gì mà tò mò về sorting vậy?"
                
                Ví dụ phong cách TỐT khi giới thiệu khóa học:
                User: "tìm khóa học dưới 300k"
                Bot: "Mình tìm thấy mấy khóa trong tầm giá này nè:\n[danh sách khóa học]\n\nNhìn qua thì khóa 'Data Structures Basic' khá ổn cho người mới bắt đầu đấy. Bạn đã có nền tảng lập trình chưa?"
                
                TRÁNH:
                - "Mình có thể hỗ trợ..." (quá formal)
                - "Bạn có thể hỏi cụ thể hơn..." (nghe như đuổi khách)
                - Lặp lại cấu trúc câu giống nhau
                """;

        String userPrompt = buildFinalUserPrompt(userMessage, intent, matchedCourses);

        try {
            String answer = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            if (answer == null || answer.isBlank()) {
                return new ChatResponse(
                    buildDynamicFallback(userMessage, intent, matchedCourses),
                    matchedCourses
                );
            }

            return new ChatResponse(answer, matchedCourses);
        } catch (Exception e) {
            return new ChatResponse(
                buildDynamicFallback(userMessage, intent, matchedCourses),
                matchedCourses
            );
        }
    }

    private String buildDynamicFallback(String userMessage, IntentInfo intent, 
                                         List<ChatResponse.CourseSearchResult> courses) {
        try {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("User message: ").append(userMessage).append("\n");
            
            if (intent.courseSearch()) {
                if (courses.isEmpty()) {
                    contextBuilder.append("Context: Không tìm thấy khóa học phù hợp.\n");
                    if (intent.maxPriceVnd() != null) {
                        contextBuilder.append("Lý do: Có thể do mức giá quá thấp hoặc chủ đề không khớp.\n");
                    }
                } else {
                    contextBuilder.append("Context: Đã tìm được ").append(courses.size())
                                  .append(" khóa học phù hợp.\n");
                }
            }
            
            String fallbackPrompt = contextBuilder.toString() + 
                "\nYêu cầu: Trả lời TỰ NHIÊN, THÂN THIỆN như trò chuyện với bạn bè. " +
                "Tránh câu rập khuôn kiểu 'Mình có thể hỗ trợ...'. " +
                "Hãy trò chuyện theo phong cách chatbot hiện đại, có cảm xúc, động viên người học.";
            
            String fallback = chatClient.prompt()
                    .system("Bạn là AI chatbot thông minh, trò chuyện tự nhiên như người thật. " +
                            "Chuyên về DSA nhưng giao tiếp rất gần gũi, thân thiện.")
                    .user(fallbackPrompt)
                    .call()
                    .content();

            if (fallback != null && !fallback.isBlank()) {
                return fallback;
            }
        } catch (Exception ignored) {
        }

        // Last resort: vẫn để AI sinh thay vì hardcode
        return "Hmm, có gì đó không ổn với hệ thống. Bạn thử hỏi lại câu khác nhé! 🤔";
    }

    private IntentInfo analyzeIntent(String userMessage) {
        try {
            String parserSystem = """
                    Bạn là bộ phân tích ý định người dùng.
                    Trả về DUY NHẤT JSON hợp lệ, không markdown:
                    {
                      "courseSearch": boolean,
                      "topic": string,
                                            "minPriceVnd": number|null,
                      "maxPriceVnd": number|null,
                      "keywords": string[]
                    }
                    Quy tắc:
                    - courseSearch=true nếu người dùng muốn tìm/đề xuất/lọc khóa học hoặc nói muốn học 1 chủ đề cụ thể.
                    - Hiểu giá kiểu: 300k, 300 nghìn, 0.3 triệu => 300000.
                                        - "dưới/không quá/tối đa" => maxPriceVnd.
                                        - "trên/từ" => minPriceVnd.
                    - keywords là từ khóa tìm kiếm khóa học (có thể gồm spring boot, graph, sort...).
                                        - Nếu không có cận dưới thì minPriceVnd = null.
                    - Nếu không có giá thì maxPriceVnd = null.
                    """;

            String raw = chatClient.prompt()
                    .system(parserSystem)
                    .user(userMessage)
                    .call()
                    .content();

            String json = extractJson(raw);
            JsonNode root = objectMapper.readTree(json);

            boolean courseSearch = root.path("courseSearch").asBoolean(false);
            String topic = root.path("topic").asText("").trim();

            BigDecimal minPrice = null;
            JsonNode minPriceNode = root.path("minPriceVnd");
            if (!minPriceNode.isMissingNode() && !minPriceNode.isNull()) {
                String digits = minPriceNode.asText("").replaceAll("[^0-9]", "");
                if (!digits.isBlank()) {
                    minPrice = new BigDecimal(digits);
                }
            }

            BigDecimal maxPrice = null;
            JsonNode priceNode = root.path("maxPriceVnd");
            if (!priceNode.isMissingNode() && !priceNode.isNull()) {
                String digits = priceNode.asText("").replaceAll("[^0-9]", "");
                if (!digits.isBlank()) {
                    maxPrice = new BigDecimal(digits);
                }
            }

            List<String> keywords = new ArrayList<>();
            JsonNode kw = root.path("keywords");
            if (kw.isArray()) {
                for (JsonNode k : kw) {
                    String v = normalizeForSearch(k.asText(""));
                    if (!v.isBlank()) {
                        keywords.add(v);
                    }
                }
            }

            if (keywords.isEmpty() && !normalizeForSearch(topic).isBlank()) {
                keywords.add(normalizeForSearch(topic));
            }

            keywords = sanitizeKeywords(keywords);

            return new IntentInfo(courseSearch, topic, minPrice, maxPrice, keywords);
        } catch (Exception ignored) {
            // fallback nhẹ khi parser lỗi
            String msg = normalizeForSearch(userMessage);
            boolean search = msg.contains("tim") || msg.contains("khoa hoc") || msg.contains("course") || msg.contains("muon hoc");
            BigDecimal minPrice = parseMinPriceFallback(msg);
            BigDecimal maxPrice = parseMaxPriceFallback(msg);
            List<String> keywords = extractKeywordsFallback(msg);
            return new IntentInfo(search, "", minPrice, maxPrice, sanitizeKeywords(keywords));
        }
    }

    private List<ChatResponse.CourseSearchResult> findCourses(IntentInfo intent) {
        List<Course> all = courseRepository.findAll();
        if (all.isEmpty()) {
            return List.of();
        }

        List<ScoredCourse> scored = new ArrayList<>();
        for (Course c : all) {
            String title = normalizeForSearch(c.getTitle());
            String desc = normalizeForSearch(c.getDescription());
            String combined = (title + " " + desc).trim();

            if (intent.minPriceVnd() != null && c.getPrice() != null && c.getPrice().compareTo(intent.minPriceVnd()) < 0) {
                continue;
            }

            if (intent.maxPriceVnd() != null && c.getPrice() != null && c.getPrice().compareTo(intent.maxPriceVnd()) > 0) {
                continue;
            }

            int score = 0;
            int hit = 0;

            for (String kw : intent.keywords()) {
                if (kw.isBlank()) {
                    continue;
                }
                if (title.contains(kw)) {
                    score += 5;
                    hit++;
                    continue;
                }
                if (combined.contains(kw)) {
                    score += 3;
                    hit++;
                    continue;
                }

                for (String part : kw.split(" ")) {
                    if (part.length() >= 3 && combined.contains(part)) {
                        score += 1;
                        hit++;
                    }
                }
            }

            if (!intent.keywords().isEmpty() && hit == 0) {
                continue;
            }

            if (intent.keywords().isEmpty()) {
                score = 1;
            }

            if (score > 0) {
                scored.add(new ScoredCourse(c, score));
            }
        }

        NumberFormat vnd = NumberFormat.getInstance(new Locale("vi", "VN"));
        return scored.stream()
                .sorted(Comparator.comparingInt(ScoredCourse::score).reversed())
                .limit(10)
                .map(s -> new ChatResponse.CourseSearchResult(
                        s.course().getCourseId(),
                        s.course().getTitle(),
                        trimDesc(s.course().getDescription()),
                        s.course().getPrice() == null ? "Liên hệ" : vnd.format(s.course().getPrice()) + " ₫",
                        s.course().getInstructor() != null ? s.course().getInstructor().getFullName() : "N/A"
                ))
                .toList();
    }

    private String buildFinalUserPrompt(String userMessage, IntentInfo intent, List<ChatResponse.CourseSearchResult> courses) {
        StringBuilder ctx = new StringBuilder();
        if (courses.isEmpty()) {
            ctx.append("(trống)");
        } else {
            for (int i = 0; i < courses.size(); i++) {
                ChatResponse.CourseSearchResult c = courses.get(i);
                ctx.append(i + 1)
                        .append(") ")
                        .append(c.getTitle())
                        .append(" | Giá: ")
                        .append(c.getPrice())
                        .append(" | Mô tả: ")
                        .append(c.getDescription() == null ? "" : c.getDescription())
                        .append("\n");
            }
        }

        return """
                USER_MESSAGE:
                %s

                INTENT:
                - courseSearch: %s
                - topic: %s
                - minPriceVnd: %s
                - maxPriceVnd: %s
                - keywords: %s

                COURSE_CONTEXT (nếu courseSearch=true):
                %s

                Hãy trả lời phù hợp intent. Nếu là courseSearch thì chỉ dùng COURSE_CONTEXT.
                """.formatted(
                userMessage,
                intent.courseSearch(),
                intent.topic().isBlank() ? "(không rõ)" : intent.topic(),
                intent.minPriceVnd() == null ? "(không giới hạn)" : intent.minPriceVnd().toPlainString(),
                intent.maxPriceVnd() == null ? "(không giới hạn)" : intent.maxPriceVnd().toPlainString(),
                intent.keywords(),
                ctx.toString().trim()
        );
    }

    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            return "{}";
        }
        String t = content.trim();
        if (t.startsWith("```") && t.endsWith("```")) {
            t = t.replaceAll("^```[a-zA-Z]*", "").replaceAll("```$", "").trim();
        }
        int s = t.indexOf('{');
        int e = t.lastIndexOf('}');
        return (s >= 0 && e > s) ? t.substring(s, e + 1) : t;
    }

    private String normalizeForSearch(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private BigDecimal parseMoneyValue(String text) {
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d+(?:\\.\\d+)?)\\s*(k|nghin|trieu|m)?")
                    .matcher(text);
            if (m.find()) {
                double n = Double.parseDouble(m.group(1));
                String u = m.group(2);
                long v;
                if (u == null) {
                    v = n < 1000 ? (long) (n * 1000) : (long) n;
                } else if (u.equals("k") || u.equals("nghin")) {
                    v = (long) (n * 1000);
                } else {
                    v = (long) (n * 1_000_000);
                }
                return BigDecimal.valueOf(v);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private BigDecimal parseMaxPriceFallback(String normalized) {
        if (normalized.contains("duoi") || normalized.contains("khong qua") || normalized.contains("toi da")) {
            return parseMoneyValue(normalized);
        }
        return null;
    }

    private BigDecimal parseMinPriceFallback(String normalized) {
        if (normalized.contains("tren") || normalized.contains("tu ")) {
            return parseMoneyValue(normalized);
        }
        return null;
    }

    private List<String> sanitizeKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        List<String> stopWords = List.of(
                "tim", "khoa", "hoc", "course", "toi", "muon", "duoi", "tren", "gia", "hoc phi",
                "khong", "qua", "toi da", "tu", "den", "la", "ve", "cho", "minh"
        );

        List<String> sanitized = new ArrayList<>();
        for (String kw : keywords) {
            String n = normalizeForSearch(kw);
            if (n.isBlank() || n.matches(".*\\d.*") || stopWords.contains(n)) {
                continue;
            }
            sanitized.add(n);
        }
        return sanitized.stream().distinct().toList();
    }

    private List<String> extractKeywordsFallback(String normalized) {
        String[] stop = {"tim", "khoa", "hoc", "course", "toi", "muon", "hoc", "duoi", "tren", "gia", "hoc", "phi", "khong", "qua"};
        List<String> stopWords = List.of(stop);

        List<String> result = new ArrayList<>();
        for (String t : normalized.split(" ")) {
            if (t.length() < 2 || stopWords.contains(t) || t.matches("\\d+")) {
                continue;
            }
            result.add(t);
        }
        if (normalized.contains("spring boot")) {
            result.add("spring boot");
        }
        return result.stream().distinct().toList();
    }

    private String trimDesc(String desc) {
        if (desc == null) {
            return "";
        }
        return desc.length() > 150 ? desc.substring(0, 150) + "..." : desc;
    }

    private record IntentInfo(boolean courseSearch, String topic, BigDecimal minPriceVnd, BigDecimal maxPriceVnd,
                              List<String> keywords) {
    }

    private record ScoredCourse(Course course, int score) {
    }
}
