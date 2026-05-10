package com.smart_hire.document.service;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoDocumentRepository extends MongoRepository<DocumentEntity, String> {

    List<DocumentEntity> findAllByOwnerId(String ownerId);

    Optional<DocumentEntity> findFirstByOwnerIdAndTypeOrderByUpdatedAtDesc(String ownerId, String type);
}
