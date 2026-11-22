
package com.karan.rag.chatbot.controller;

import com.karan.rag.chatbot.service.PdfIngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class PdfUploadController {

    private final PdfIngestionService pdfIngestionService;

    public PdfUploadController(
            PdfIngestionService pdfIngestionService) {
        this.pdfIngestionService = pdfIngestionService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file) {

        try {
            int chunks = pdfIngestionService.ingest(file);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "filename", file.getOriginalFilename(),
                    "chunksProcessed", chunks
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(Map.of(
                    "status", "ERROR",
                    "message", "Could not read the uploaded PDF"
            ));
        }
    }
}
