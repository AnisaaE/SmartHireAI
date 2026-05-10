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
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerListByOwnerApiTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DocumentController documentController = new DocumentController(documentService);
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    void shouldListDocumentsByOwner() throws Exception {
        when(documentService.listDocumentsByOwner("candidate-1"))
                .thenReturn(List.of(new DocumentRecord(
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
                )));

        mockMvc.perform(get("/api/documents/owner/candidate-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("doc-1"))
                .andExpect(jsonPath("$[0].ownerId").value("candidate-1"))
                .andExpect(jsonPath("$[0].title").value("Java Developer CV"));
    }
}
