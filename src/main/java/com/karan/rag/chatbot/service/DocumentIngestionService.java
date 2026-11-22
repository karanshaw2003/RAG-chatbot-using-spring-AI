
package com.karan.rag.chatbot.service;

import com.karan.rag.chatbot.dto.IngestRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
    }

    public int ingest(IngestRequest request) {

        // 1. Convert the submitted text into a Spring AI Document.
        Document document = new Document(
                request.content(),
                Map.of("documentName", request.documentName())
        );

        // 2. Split the document into smaller chunks.
        List<Document> chunks = textSplitter.apply(
                List.of(document)
        );

        // 3. Store chunks and their embeddings in PostgreSQL.
        // VectorStore uses the configured EmbeddingModel.
        vectorStore.add(chunks);

        // 4. Return the number of chunks processed.
        return chunks.size();
    }
}
