
package com.karan.rag.chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record IngestRequest(
        @NotBlank(message = "Document name is required")
        String documentName,

        @NotBlank(message = "Document content is required")
        String content
) {
}
