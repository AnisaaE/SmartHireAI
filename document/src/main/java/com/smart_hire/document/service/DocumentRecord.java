package com.smart_hire.document.service;

public record DocumentRecord(
        String id,
        String ownerId,
        String type,
        String title,
        String fileName,
        String rawTextContent
) {
}
