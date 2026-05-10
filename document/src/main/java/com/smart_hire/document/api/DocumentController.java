package com.smart_hire.document.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
class DocumentController {

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    void uploadDocument(
            @RequestParam String ownerId,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam MultipartFile file
    ) {
    }

    @GetMapping("/{id}")
    DocumentMetadataResponse getDocumentMetadata(@PathVariable String id) {
        return sampleMetadata(id);
    }

    @GetMapping("/owner/{userId}")
    List<DocumentMetadataResponse> listDocumentsByOwner(@PathVariable String userId) {
        return sampleDocumentsByOwner(userId);
    }

    @GetMapping("/cv/{candidateId}")
    DocumentMetadataResponse getActiveCv(@PathVariable String candidateId) {
        return sampleActiveCv(candidateId);
    }

    private DocumentMetadataResponse sampleMetadata(String id) {
        return new DocumentMetadataResponse(id, "candidate-1", "CV", "Java Developer CV");
    }

    private List<DocumentMetadataResponse> sampleDocumentsByOwner(String userId) {
        return List.of(sampleMetadata("doc-1"));
    }

    private DocumentMetadataResponse sampleActiveCv(String candidateId) {
        return sampleMetadata("doc-1");
    }
}
