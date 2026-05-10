package com.smart_hire.document.service.impl;

import com.smart_hire.document.service.DocumentRecord;
import com.smart_hire.document.service.DocumentRepository;
import com.smart_hire.document.service.DocumentService;
import com.smart_hire.document.service.DocumentTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractor documentTextExtractor;

    @Override
    public DocumentRecord uploadDocument(String ownerId, String type, String title, MultipartFile file) {
        String rawTextContent = documentTextExtractor.extract(file);
        return documentRepository.save(buildDocumentRecord(ownerId, type, title, file, rawTextContent));
    }

    @Override
    public DocumentRecord getDocumentMetadata(String id) {
        return documentRepository.findById(id).orElseThrow();
    }

    private DocumentRecord buildDocumentRecord(
            String ownerId,
            String type,
            String title,
            MultipartFile file,
            String rawTextContent
    ) {
        return new DocumentRecord(
                null,
                ownerId,
                type,
                title,
                file.getOriginalFilename(),
                rawTextContent
        );
    }
}
