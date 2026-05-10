package com.smart_hire.document.service.impl;

import com.smart_hire.document.service.DocumentRecord;
import com.smart_hire.document.service.DocumentRepository;
import com.smart_hire.document.service.DocumentService;
import com.smart_hire.document.service.DocumentTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractor documentTextExtractor;

    @Override
    public DocumentRecord uploadDocument(String ownerId, String type, String title, MultipartFile file) {
        byte[] fileBytes = readFileBytes(file);
        String rawTextContent = documentTextExtractor.extract(file);

        DocumentRecord document = new DocumentRecord(
                null,
                ownerId,
                type,
                title,
                file.getOriginalFilename(),
                rawTextContent,
                fileBytes,
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );

        return documentRepository.save(document);
    }

    @Override
    public DocumentRecord getDocumentMetadata(String id) {
        return getDocumentById(id);
    }

    @Override
    public List<DocumentRecord> listDocumentsByOwner(String ownerId) {
        return documentRepository.findByOwnerId(ownerId);
    }

    @Override
    public DocumentRecord getActiveCv(String candidateId) {
        return documentRepository.findActiveCv(candidateId).orElseThrow();
    }

    @Override
    public String getDocumentContent(String id) {
        return getDocumentById(id).rawTextContent();
    }

    @Override
    public DocumentRecord updateDocumentMetadata(String id, String title, String type) {
        DocumentRecord existing = getDocumentById(id);
        DocumentRecord updated = new DocumentRecord(
                existing.id(),
                existing.ownerId(),
                type,
                title,
                existing.fileName(),
                existing.rawTextContent(),
                existing.fileContent(),
                existing.status(),
                existing.createdAt(),
                Instant.now()
        );

        return documentRepository.save(updated);
    }

    @Override
    public DocumentRecord updateDocumentContent(String id, String rawTextContent) {
        DocumentRecord existing = getDocumentById(id);
        DocumentRecord updated = new DocumentRecord(
                existing.id(),
                existing.ownerId(),
                existing.type(),
                existing.title(),
                existing.fileName(),
                rawTextContent,
                existing.fileContent(),
                existing.status(),
                existing.createdAt(),
                Instant.now()
        );

        return documentRepository.save(updated);
    }

    @Override
    public DocumentRecord reprocessDocument(String id) {
        DocumentRecord existing = getDocumentById(id);
        if (existing.fileContent() == null) {
            throw new IllegalStateException("Cannot reprocess document without stored file content");
        }

        String rawTextContent = documentTextExtractor.extract(existing.fileContent(), existing.fileName());
        DocumentRecord updated = new DocumentRecord(
                existing.id(),
                existing.ownerId(),
                existing.type(),
                existing.title(),
                existing.fileName(),
                rawTextContent,
                existing.fileContent(),
                "REPROCESSED",
                existing.createdAt(),
                Instant.now()
        );

        return documentRepository.save(updated);
    }

    @Override
    public void deleteDocument(String id) {
        documentRepository.deleteById(id);
    }

    private DocumentRecord getDocumentById(String id) {
        return documentRepository.findById(id).orElseThrow();
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        }
        catch (IOException ex) {
            throw new IllegalStateException("Failed to read uploaded document", ex);
        }
    }
}
