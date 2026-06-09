package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentRecord;
import com.smart_hire.document.service.DocumentService;
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

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ViewMapper<DocumentRecord, DocumentMetadataResponse> documentMetadataMapper;
    private final ViewMapper<DocumentRecord, DocumentContentResponse> documentContentMapper;

    public DocumentController(
            DocumentService documentService,
            ViewMapper<DocumentRecord, DocumentMetadataResponse> documentMetadataMapper,
            ViewMapper<DocumentRecord, DocumentContentResponse> documentContentMapper
    ) {
        this.documentService = documentService;
        this.documentMetadataMapper = documentMetadataMapper;
        this.documentContentMapper = documentContentMapper;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentMetadataResponse uploadDocument(
            @RequestParam String ownerId,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam MultipartFile file
    ) {
        DocumentRecord document = documentService.uploadDocument(ownerId, type, title, file);
        return documentMetadataMapper.map(document);
    }

    @GetMapping("/{id}")
    public DocumentMetadataResponse getDocumentMetadata(@PathVariable String id) {
        return documentMetadataMapper.map(documentService.getDocumentMetadata(id));
    }

    @GetMapping("/owner/{userId}")
    public List<DocumentMetadataResponse> listDocumentsByOwner(@PathVariable String userId) {
        return documentMetadataMapper.mapAll(documentService.listDocumentsByOwner(userId));
    }

    @GetMapping("/owner/{userId}/collection")
    public CollectionResponse<DocumentMetadataResponse> listDocumentsByOwnerAsCollection(@PathVariable String userId) {
        List<DocumentMetadataResponse> items = documentMetadataMapper.mapAll(documentService.listDocumentsByOwner(userId));
        return new CollectionResponse<>(items, items.size());
    }

    @GetMapping("/cv/{candidateId}")
    public DocumentMetadataResponse getActiveCv(@PathVariable String candidateId) {
        return documentMetadataMapper.map(documentService.getActiveCv(candidateId));
    }

    @GetMapping("/content/{id}")
    public DocumentContentResponse getDocumentContent(@PathVariable String id) {
        return documentContentMapper.map(documentService.getDocumentMetadata(id));
    }

    @PutMapping("/{id}")
    public DocumentMetadataResponse updateDocumentMetadata(
            @PathVariable String id,
            @RequestBody UpdateDocumentMetadataRequest request
    ) {
        return documentMetadataMapper.map(documentService.updateDocumentMetadata(id, request.title(), request.type()));
    }

    @PutMapping("/{id}/content")
    public DocumentContentResponse updateDocumentContent(
            @PathVariable String id,
            @RequestBody UpdateDocumentContentRequest request
    ) {
        return documentContentMapper.map(documentService.updateDocumentContent(id, request.rawTextContent()));
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
}