package com.smart_hire.document.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentTextExtractor {

    String extract(MultipartFile file);
}
