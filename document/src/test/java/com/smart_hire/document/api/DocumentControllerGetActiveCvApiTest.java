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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerGetActiveCvApiTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DocumentController documentController = new DocumentController(documentService);
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    void shouldGetActiveCvByCandidateId() throws Exception {
        when(documentService.getActiveCv("candidate-1"))
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

        mockMvc.perform(get("/api/documents/cv/candidate-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc-1"))
                .andExpect(jsonPath("$.ownerId").value("candidate-1"))
                .andExpect(jsonPath("$.type").value("CV"));
    }
}
