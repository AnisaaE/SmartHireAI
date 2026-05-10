package com.smart_hire.document.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerGetMetadataApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetDocumentMetadataById() throws Exception {
        mockMvc.perform(get("/api/documents/doc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc-1"))
                .andExpect(jsonPath("$.ownerId").value("candidate-1"))
                .andExpect(jsonPath("$.type").value("CV"))
                .andExpect(jsonPath("$.title").value("Java Developer CV"));
    }
}
