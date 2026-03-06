# 🤖 Hướng Dẫn Sử Dụng Tính Năng AI Assistant

## 📋 Tổng Quan

Tính năng AI Assistant được tích hợp vào DSA Learning Platform để giúp học viên tìm kiếm khóa học thông minh bằng ngôn ngữ tự nhiên. Sử dụng Spring AI và OpenAI GPT-4 để hiểu ngữ cảnh và đề xuất khóa học phù hợp.

## 🚀 Cài Đặt

### 1. Cấu Hình OpenAI API Key

Để sử dụng tính năng AI, bạn cần có OpenAI API key:

1. Đăng ký tài khoản tại: https://platform.openai.com/
2. Tạo API key tại: https://platform.openai.com/api-keys
3. Copy file `.env.example` thành `.env`:
   ```bash
   cp .env.example .env
   ```
4. Mở file `.env` và thay thế API key:
   ```properties
   OPENAI_API_KEY=sk-proj-your-actual-api-key-here
   ```

### 2. Cấu Hình (application.properties)

File đã được cấu hình sẵn với các tham số:

```properties
# Spring AI OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY:your-api-key-here}
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7
```

**Lưu ý:**
- `gpt-4o-mini`: Model tiết kiệm chi phí, phù hợp cho development
- `temperature=0.7`: Cân bằng giữa creativity và consistency

### 3. Dependencies Đã Được Thêm

```xml
<!-- Spring AI OpenAI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.0-M5</version>
</dependency>
```

## 💡 Cách Sử Dụng

### 1. Giao Diện Người Dùng

- **Vị trí**: Trang Dashboard (`/dashboard`)
- **Nút Chat**: Floating button góc dưới bên phải màn hình (icon robot 🤖)
- **Click** vào nút để mở chat box

### 2. Ví Dụ Câu Hỏi

**Tìm kiếm theo chủ đề và giá:**
```
Khóa học về thuật toán sắp xếp với học phí dưới 300 nghìn
```

**Tìm kiếm theo chủ đề:**
```
Tìm khóa học về cấu trúc dữ liệu
```

**Tìm kiếm theo giá:**
```
Khóa học dưới 500 nghìn
```

**Tìm kiếm kết hợp:**
```
Khóa học về đồ thị và thuật toán tìm kiếm, giá không quá 400 nghìn
```

### 3. Kết Quả

AI sẽ:
1. Phân tích yêu cầu của bạn
2. Tìm kiếm trong database
3. Trả về danh sách khóa học phù hợp với thông tin:
   - Tên khóa học
   - Mô tả ngắn
   - Giá (định dạng VND)
   - Giảng viên
   - Link xem chi tiết

## 🏗️ Kiến Trúc Kỹ Thuật

### 1. Components

```
┌─────────────────────────────────────────┐
│         Frontend (dashboard.html)       │
│  • Chat UI với floating button          │
│  • JavaScript fetch API                 │
└────────────────┬────────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────────┐
│    AIAssistantController                │
│  • POST /api/ai/chat                    │
│  • Validation & Error Handling          │
└────────────────┬────────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────────┐
│    AIAssistantService                   │
│  • ChatClient với Tool Calling          │
│  • Natural Language Processing          │
│  • Course Search Logic                  │
└────────────────┬────────────────────────┘
                 │
          ┌──────┴──────┐
          ↓             ↓
┌──────────────┐  ┌────────────────┐
│  OpenAI API  │  │ CourseRepository│
│  GPT-4 Mini  │  │   (Database)   │
└──────────────┘  └────────────────┘
```

### 2. AI Tool (Function Calling)

```java
@Tool(description = "Tìm kiếm khóa học...")
public String searchCourses(
    String keyword,    // Từ khóa tìm kiếm
    Integer maxPrice,  // Giá tối đa
    Integer minPrice   // Giá tối thiểu
)
```

Spring AI tự động:
- Parse user message
- Extract parameters (keyword, price range)
- Call tool với đúng arguments
- Format kết quả trả về user

## 📊 API Endpoints

### POST `/api/ai/chat`

**Request:**
```json
{
  "message": "Khóa học về thuật toán sắp xếp với học phí dưới 300 nghìn"
}
```

**Response:**
```json
{
  "message": "Tìm thấy 3 khóa học phù hợp với yêu cầu của bạn...",
  "courses": [
    {
      "courseId": 1,
      "title": "Thuật Toán Sắp Xếp Nâng Cao",
      "description": "Học các thuật toán...",
      "price": "250,000 ₫",
      "instructor": "Nguyễn Văn A"
    }
  ]
}
```

## 🔧 Troubleshooting

### Lỗi: "OpenAI API key is not configured"

**Nguyên nhân**: Chưa cấu hình API key

**Giải pháp**:
1. Kiểm tra file `.env` có tồn tại
2. Đảm bảo `OPENAI_API_KEY` được set đúng
3. Restart ứng dụng

### Lỗi: "Rate limit exceeded"

**Nguyên nhân**: Đã vượt quá giới hạn request của OpenAI

**Giải pháp**:
1. Đợi 1 phút rồi thử lại
2. Kiểm tra quota tại: https://platform.openai.com/usage
3. Upgrade plan nếu cần

### Lỗi: "Network timeout"

**Nguyên nhân**: Kết nối tới OpenAI API bị timeout

**Giải pháp**:
1. Kiểm tra internet connection
2. Kiểm tra firewall/proxy settings
3. Thử lại sau

## 🎯 Best Practices

### 1. Tối Ưu Chi Phí

- Sử dụng `gpt-4o-mini` thay vì `gpt-4` cho dev/test
- Cache kết quả tìm kiếm phổ biến
- Implement rate limiting

### 2. Cải Thiện Trải Nghiệm

- Clear error messages
- Loading indicators
- Sample questions/hints
- Chat history (future enhancement)

### 3. Bảo Mật

- **KHÔNG** commit API key vào Git
- Sử dụng environment variables
- Implement authentication cho API
- Rate limiting per user

## 🚀 Nâng Cấp Tương Lai

### Phase 2 - Enhanced Features:
- [ ] Multi-turn conversations (chat history)
- [ ] Course recommendations based on learning history
- [ ] Voice input/output
- [ ] Multi-language support

### Phase 3 - Advanced AI:
- [ ] Personalized learning paths
- [ ] Code review assistant
- [ ] Assignment help (with hints)
- [ ] Study schedule optimizer

### Phase 4 - MCP Integration:
- [ ] Build MCP Server to expose course catalog
- [ ] Connect with external learning platforms
- [ ] Unified search across multiple platforms

## 📚 Tài Liệu Tham Khảo

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API Docs](https://platform.openai.com/docs)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Spring AI Function Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)

## 📞 Hỗ Trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra logs trong console
2. Xem API response trong Network tab
3. Đọc error messages cẩn thận
4. Liên hệ team nếu cần hỗ trợ thêm

---

**Lưu ý:** Đây là tính năng experimental sử dụng AI. Chi phí API sẽ phụ thuộc vào usage. Monitor usage tại: https://platform.openai.com/usage
