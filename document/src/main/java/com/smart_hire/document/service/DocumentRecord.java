package com.smart_hire.document.service;

import java.time.Instant;

public record DocumentRecord(
        String id,
        String ownerId,
        String type,
        String title,
        String fileName,
        String rawTextContent,
        byte[] fileContent,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
