package com.smart_hire.document.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerListByOwnerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListDocumentsByOwner() throws Exception {
        mockMvc.perform(get("/api/documents/owner/candidate-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("doc-1"))
                .andExpect(jsonPath("$[0].ownerId").value("candidate-1"))
                .andExpect(jsonPath("$[0].title").value("Java Developer CV"));
    }
}
