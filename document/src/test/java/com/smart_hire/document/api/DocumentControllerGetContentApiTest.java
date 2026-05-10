package com.smart_hire.document.api;

import com.smart_hire.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerGetContentApiTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DocumentController documentController = new DocumentController(documentService);
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    void shouldGetDocumentContentById() throws Exception {
        when(documentService.getDocumentContent("doc-1")).thenReturn("Extracted resume text");

        mockMvc.perform(get("/api/documents/content/doc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("doc-1"))
                .andExpect(jsonPath("$.rawTextContent").value("Extracted resume text"));
    }
}
