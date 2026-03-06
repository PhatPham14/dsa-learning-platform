package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.dto.request.ChatRequest;
import com.edu.dsalearningplatform.dto.response.ChatResponse;
import com.edu.dsalearningplatform.service.AIAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Assistant", description = "AI-powered learning assistant endpoints")
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;

    public AIAssistantController(AIAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI assistant", 
               description = "AI chatbot for learning support. It only searches courses when user explicitly asks to find/recommend courses.")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            ChatResponse response = aiAssistantService.searchCourses(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse("Đã xảy ra lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
