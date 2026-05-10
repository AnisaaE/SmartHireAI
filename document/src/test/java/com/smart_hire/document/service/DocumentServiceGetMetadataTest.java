package com.smart_hire.document.service;

import com.smart_hire.document.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceGetMetadataTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTextExtractor documentTextExtractor;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void shouldGetDocumentMetadataById() {
        DocumentRecord expected = new DocumentRecord(
                "doc-1",
                "candidate-1",
                "CV",
                "Java Developer CV",
                "resume.pdf",
                "Extracted resume text",
                new byte[0],
                "ACTIVE",
                java.time.Instant.now(),
                java.time.Instant.now()
        );

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(expected));

        DocumentRecord result = documentService.getDocumentMetadata("doc-1");

        assertThat(result).isEqualTo(expected);
        verify(documentRepository).findById("doc-1");
    }
}
