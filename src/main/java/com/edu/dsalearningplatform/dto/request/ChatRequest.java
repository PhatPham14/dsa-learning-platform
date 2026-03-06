package com.edu.dsalearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {
    
    @NotBlank(message = "Message is required")
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
