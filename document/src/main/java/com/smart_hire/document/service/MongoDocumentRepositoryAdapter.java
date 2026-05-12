package com.smart_hire.document.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MongoDocumentRepositoryAdapter implements DocumentRepository {

    private final MongoDocumentRepository mongoRepository;

    @Override
    public DocumentRecord save(DocumentRecord documentRecord) {
        DocumentEntity entity = toEntity(documentRecord);
        DocumentEntity saved = mongoRepository.save(entity);
        return toRecord(saved);
    }

    @Override
    public Optional<DocumentRecord> findById(String id) {
        return mongoRepository.findById(id)
                .filter(entity -> !"DELETED".equalsIgnoreCase(entity.getStatus()))
                .map(this::toRecord);
    }

    @Override
    public List<DocumentRecord> findByOwnerId(String ownerId) {
        return mongoRepository.findAllByOwnerIdAndStatusNot(ownerId, "DELETED").stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DocumentRecord> findActiveCv(String ownerId) {
        return mongoRepository.findFirstByOwnerIdAndTypeAndStatusInOrderByUpdatedAtDesc(
                        ownerId,
                        "CV",
                        List.of("ACTIVE", "REPROCESSED")
                )
                .map(this::toRecord);
    }

    private DocumentEntity toEntity(DocumentRecord record) {
        return DocumentEntity.builder()
                .id(record.id())
                .ownerId(record.ownerId())
                .type(record.type())
                .title(record.title())
                .fileName(record.fileName())
                .rawTextContent(record.rawTextContent())
                .fileContent(record.fileContent())
                .status(record.status())
                .createdAt(record.createdAt())
                .updatedAt(record.updatedAt())
                .build();
    }

    private DocumentRecord toRecord(DocumentEntity entity) {
        return new DocumentRecord(
                entity.getId(),
                entity.getOwnerId(),
                entity.getType(),
                entity.getTitle(),
                entity.getFileName(),
                entity.getRawTextContent(),
                entity.getFileContent(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
