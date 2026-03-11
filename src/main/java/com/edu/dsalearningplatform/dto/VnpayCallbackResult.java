package com.edu.dsalearningplatform.dto;

public record VnpayCallbackResult(
        boolean validSignature,
        boolean success,
        Long courseId,
        String rspCode,
        String message
) {
}
