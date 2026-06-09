package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentRecord;
import org.springframework.stereotype.Component;

@Component
class DocumentContentMapper implements ViewMapper<DocumentRecord, DocumentContentResponse> {

    @Override
    public DocumentContentResponse map(DocumentRecord document) {
        return new DocumentContentResponse(document.id(), document.rawTextContent());
    }
}
