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
class DocumentControllerUpdateContentApiTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DocumentController documentController = new DocumentController(documentService);
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    void shouldUpdateDocumentContent() throws Exception {
        when(documentService.updateDocumentContent(eq("doc-1"), eq("Updated extracted text")))
                .thenReturn(new DocumentRecord(
                        "doc-1",
                        "candidate-1",
                        "CV",
                        "Java Developer CV",
                        "resume.pdf",
                        "Updated extracted text",
                        new byte[0],
                        "ACTIVE",
                        Instant.now(),
                        Instant.now()
                ));

        mockMvc.perform(put("/api/documents/doc-1/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawTextContent": "Updated extracted text"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("doc-1"))
                .andExpect(jsonPath("$.rawTextContent").value("Updated extracted text"));
    }
}
