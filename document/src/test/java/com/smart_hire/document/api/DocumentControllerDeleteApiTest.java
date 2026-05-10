package com.smart_hire.document.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerDeleteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDeleteDocument() throws Exception {
        mockMvc.perform(delete("/api/documents/doc-1"))
                .andExpect(status().isNoContent());
    }
}
