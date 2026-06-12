package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentRecord;
import com.smart_hire.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerReprocessApiTest {

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
    void shouldReprocessDocument() throws Exception {
        when(documentService.reprocessDocument("doc-1"))
                .thenReturn(new DocumentRecord(
                        "doc-1",
                        "candidate-1",
                        "CV",
                        "Java Developer CV",
                        "resume.pdf",
                        "Extracted resume text",
                        new byte[0],
                        "REPROCESSED",
                        Instant.now(),
                        Instant.now()
                ));

        mockMvc.perform(put("/api/documents/doc-1/reprocess"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("doc-1"))
                .andExpect(jsonPath("$.status").value("REPROCESSED"));
    }
}
