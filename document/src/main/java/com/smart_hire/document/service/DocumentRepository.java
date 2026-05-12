package com.smart_hire.document.service;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {

    DocumentRecord save(DocumentRecord documentRecord);

    Optional<DocumentRecord> findById(String id);

    List<DocumentRecord> findByOwnerId(String ownerId);

    Optional<DocumentRecord> findActiveCv(String ownerId);
}
