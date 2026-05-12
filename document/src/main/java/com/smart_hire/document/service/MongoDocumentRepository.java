package com.smart_hire.document.service;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoDocumentRepository extends MongoRepository<DocumentEntity, String> {

    List<DocumentEntity> findAllByOwnerIdAndStatusNot(String ownerId, String status);

    Optional<DocumentEntity> findFirstByOwnerIdAndTypeAndStatusInOrderByUpdatedAtDesc(String ownerId, String type, List<String> statuses);
}
