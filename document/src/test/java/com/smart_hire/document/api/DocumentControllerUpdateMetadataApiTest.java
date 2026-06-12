package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentRecord;
import com.smart_hire.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerUpdateMetadataApiTest {

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
    void shouldUpdateDocumentMetadata() throws Exception {
        when(documentService.updateDocumentMetadata(eq("doc-1"), eq("Updated CV"), eq("CV")))
                .thenReturn(new DocumentRecord(
                        "doc-1",
                        "candidate-1",
                        "CV",
                        "Updated CV",
                        "resume.pdf",
                        "Extracted resume text",
                        new byte[0],
                        "ACTIVE",
                        Instant.now(),
                        Instant.now()
                ));

        mockMvc.perform(put("/api/documents/doc-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated CV",
                                  "type": "CV"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc-1"))
                .andExpect(jsonPath("$.title").value("Updated CV"))
                .andExpect(jsonPath("$.type").value("CV"));
    }
}
