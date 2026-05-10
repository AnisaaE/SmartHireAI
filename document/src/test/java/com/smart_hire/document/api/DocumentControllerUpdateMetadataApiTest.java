package com.smart_hire.document.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerUpdateMetadataApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUpdateDocumentMetadata() throws Exception {
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
