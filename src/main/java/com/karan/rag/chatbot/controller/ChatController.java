package com.karan.rag.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatController(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(
            @RequestBody Map<String, String> request) {

        String question = request.get("question");

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty");
        }

        // 1. Retrieve the most relevant document chunks.
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .build()
        );

        // 2. Combine retrieved chunks into context.
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 3. Ask the LLM to answer using that context.
        String answer = chatClient.prompt()
                .system("""
                        You are a helpful document-based assistant.
                        Answer using only the supplied context.
                        If the context does not contain the answer,
                        say: "I couldn't find that information
                        in the provided documents."
                        Do not invent facts.
                        """)
                .user("""
                        Context:
                        %s

                        Question:
                        %s
                        """.formatted(context, question))
                .call()
                .content();

        return Map.of(
                "answer", answer,
                "retrievedChunks", String.valueOf(documents.size())
        );
    }
}
