package com.smart_hire.document.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "documents")
public class DocumentEntity {

    @Id
    private String id;

    private String ownerId;

    private String type;

    private String title;

    private String fileName;

    private String rawTextContent;

    private byte[] fileContent;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}
