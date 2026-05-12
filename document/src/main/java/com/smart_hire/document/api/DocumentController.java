package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentRecord;
import com.smart_hire.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentMetadataResponse uploadDocument(
            @RequestParam String ownerId,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam MultipartFile file
    ) {
        DocumentRecord document = documentService.uploadDocument(ownerId, type, title, file);
        return toMetadataResponse(document);
    }

    @GetMapping("/{id}")
    public DocumentMetadataResponse getDocumentMetadata(@PathVariable String id) {
        return toMetadataResponse(documentService.getDocumentMetadata(id));
    }

    @GetMapping("/owner/{userId}")
    public List<DocumentMetadataResponse> listDocumentsByOwner(@PathVariable String userId) {
        return documentService.listDocumentsByOwner(userId).stream()
                .map(this::toMetadataResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/cv/{candidateId}")
    public DocumentMetadataResponse getActiveCv(@PathVariable String candidateId) {
        return toMetadataResponse(documentService.getActiveCv(candidateId));
    }

    @GetMapping("/content/{id}")
    public DocumentContentResponse getDocumentContent(@PathVariable String id) {
        return new DocumentContentResponse(id, documentService.getDocumentContent(id));
    }

    @PutMapping("/{id}")
    public DocumentMetadataResponse updateDocumentMetadata(
            @PathVariable String id,
            @RequestBody UpdateDocumentMetadataRequest request
    ) {
        return toMetadataResponse(documentService.updateDocumentMetadata(id, request.title(), request.type()));
    }

    @PutMapping("/{id}/content")
    public DocumentContentResponse updateDocumentContent(
            @PathVariable String id,
            @RequestBody UpdateDocumentContentRequest request
    ) {
        return new DocumentContentResponse(id, documentService.updateDocumentContent(id, request.rawTextContent()).rawTextContent());
    }

    @PutMapping("/{id}/reprocess")
    public DocumentReprocessResponse reprocessDocument(@PathVariable String id) {
        return new DocumentReprocessResponse(id, documentService.reprocessDocument(id).status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
    }

    private DocumentMetadataResponse toMetadataResponse(DocumentRecord document) {
        return new DocumentMetadataResponse(document.id(), document.ownerId(), document.type(), document.title(), document.status());
    }
}
