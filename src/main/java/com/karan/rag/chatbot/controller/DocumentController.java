package com.karan.rag.chatbot.controller;
import com.karan.rag.chatbot.dto.IngestRequest;
import com.karan.rag.chatbot.service.DocumentIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    public DocumentController(
            DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(
            @Valid @RequestBody IngestRequest request) {

        int chunksProcessed = ingestionService.ingest(request);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "documentName", request.documentName(),
                "chunksProcessed", chunksProcessed
        ));
    }
}

