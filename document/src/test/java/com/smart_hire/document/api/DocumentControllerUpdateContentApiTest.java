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
class DocumentControllerUpdateContentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUpdateDocumentContent() throws Exception {
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
