package com.smart_hire.document.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class DocumentTextExtractorImpl implements DocumentTextExtractor {

    private final Tika tika = new Tika();

    @Override
    public String extract(MultipartFile file) {
        try {
            return extract(file.getBytes(), file.getOriginalFilename());
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unable to read uploaded document", ex);
        }
    }

    @Override
    public String extract(byte[] fileBytes, String originalFilename) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            return tika.parseToString(inputStream);
        }
        catch (IOException | TikaException ex) {
            throw new IllegalStateException("Unable to extract text from document", ex);
        }
    }
}
