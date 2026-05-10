package com.smart_hire.document.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerGetContentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetDocumentContentById() throws Exception {
        mockMvc.perform(get("/api/documents/content/doc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("doc-1"))
                .andExpect(jsonPath("$.rawTextContent").value("Extracted resume text"));
    }
}
