package com.smart_hire.document.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    DocumentRecord uploadDocument(String ownerId, String type, String title, MultipartFile file);

    DocumentRecord getDocumentMetadata(String id);
}
