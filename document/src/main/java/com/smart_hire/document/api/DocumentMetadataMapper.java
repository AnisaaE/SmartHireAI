package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentRecord;
import org.springframework.stereotype.Component;

@Component
class DocumentMetadataMapper implements ViewMapper<DocumentRecord, DocumentMetadataResponse> {

    @Override
    public DocumentMetadataResponse map(DocumentRecord document) {
        return new DocumentMetadataResponse(
                document.id(),
                document.ownerId(),
                document.type(),
                document.title(),
                document.status()
        );
    }
}
