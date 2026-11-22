
package com.karan.rag.chatbot.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PdfIngestionService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter =
            new TokenTextSplitter();

    public PdfIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingest(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a non-empty PDF");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "PDF must be 10 MB or smaller");
        }

        String filename = file.getOriginalFilename();

        if (filename == null
                || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Only PDF files are supported");
        }

        byte[] bytes = file.getBytes();

        // Basic check that the file begins with a PDF signature.
        if (bytes.length < 5
                || bytes[0] != '%'
                || bytes[1] != 'P'
                || bytes[2] != 'D'
                || bytes[3] != 'F'
                || bytes[4] != '-') {
            throw new IllegalArgumentException(
                    "The uploaded file is not a valid PDF");
        }

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        // Extract text from the PDF.
        List<Document> extractedDocuments =
                new TikaDocumentReader(resource).read();

        if (extractedDocuments.isEmpty()
                || extractedDocuments.stream()
                .allMatch(doc -> doc.getText() == null
                        || doc.getText().isBlank())) {
            throw new IllegalArgumentException(
                    "No readable text found in the PDF. "
                            + "It may be a scanned document.");
        }

        // Attach source metadata to every extracted document.
        List<Document> enrichedDocuments =
                extractedDocuments.stream()
                        .map(doc -> new Document(
                                doc.getText(),
                                Map.of("documentName", filename)))
                        .toList();

        // Split extracted text into smaller chunks.
        List<Document> chunks =
                textSplitter.apply(enrichedDocuments);

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No text chunks could be created");
        }

        // Generate embeddings and save them in pgvector.
        vectorStore.add(chunks);

        return chunks.size();
    }
}
