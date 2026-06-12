package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentRecord;
import com.smart_hire.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerUploadApiTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DocumentController documentController = new DocumentController(
                documentService,
                new DocumentMetadataMapper(),
                new DocumentContentMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    void shouldUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy-pdf-content".getBytes()
        );

        when(documentService.uploadDocument(anyString(), anyString(), anyString(), any(MultipartFile.class)))
                .thenReturn(new DocumentRecord(
                        "doc-1",
                        "candidate-1",
                        "CV",
                        "Java Developer CV",
                        "resume.pdf",
                        "Extracted resume text",
                        new byte[0],
                        "ACTIVE",
                        Instant.now(),
                        Instant.now()
                ));

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("ownerId", "candidate-1")
                        .param("type", "CV")
                        .param("title", "Java Developer CV"))
                .andExpect(status().isCreated());
    }
}
