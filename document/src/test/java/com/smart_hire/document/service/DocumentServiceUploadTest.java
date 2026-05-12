package com.smart_hire.document.service;

import com.smart_hire.document.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceUploadTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTextExtractor documentTextExtractor;

    @Mock
    private DocumentApplicationClient documentApplicationClient;

    @Mock
    private DocumentAnalysisClient documentAnalysisClient;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void shouldUploadDocument() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy-pdf-content".getBytes()
        );

        when(documentTextExtractor.extract(file)).thenReturn("Extracted resume text");
        when(documentRepository.save(org.mockito.ArgumentMatchers.any(DocumentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentRecord result = documentService.uploadDocument("candidate-1", "CV", "Java Developer CV", file);

        assertThat(result.ownerId()).isEqualTo("candidate-1");
        assertThat(result.type()).isEqualTo("CV");
        assertThat(result.title()).isEqualTo("Java Developer CV");
        assertThat(result.fileName()).isEqualTo("resume.pdf");
        assertThat(result.rawTextContent()).isEqualTo("Extracted resume text");

        verify(documentTextExtractor).extract(file);
        verify(documentRepository).save(result);
    }
}
