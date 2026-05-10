package com.smart_hire.document.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentRecord uploadDocument(String ownerId, String type, String title, MultipartFile file);

    DocumentRecord getDocumentMetadata(String id);

    List<DocumentRecord> listDocumentsByOwner(String ownerId);

    DocumentRecord getActiveCv(String candidateId);

    String getDocumentContent(String id);

    DocumentRecord updateDocumentMetadata(String id, String title, String type);

    DocumentRecord updateDocumentContent(String id, String rawTextContent);

    DocumentRecord reprocessDocument(String id);

    void deleteDocument(String id);
}
