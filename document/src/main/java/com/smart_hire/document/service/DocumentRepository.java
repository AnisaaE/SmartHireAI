package com.smart_hire.document.service;

import java.util.Optional;

public interface DocumentRepository {

    DocumentRecord save(DocumentRecord documentRecord);

    Optional<DocumentRecord> findById(String id);
}
