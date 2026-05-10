package com.smart_hire.document.api;

record DocumentContentResponse(
        String documentId,
        String rawTextContent
) {
}
