package com.smart_hire.document.api;

record DocumentMetadataResponse(
        String id,
        String ownerId,
        String type,
        String title,
        String status
) {
}
