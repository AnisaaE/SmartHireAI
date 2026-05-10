package com.smart_hire.document.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
class DocumentController {

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    void uploadDocument(
            @RequestParam String ownerId,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam MultipartFile file
    ) {
    }
}
