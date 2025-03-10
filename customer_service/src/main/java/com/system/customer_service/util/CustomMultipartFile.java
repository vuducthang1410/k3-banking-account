package com.system.customer_service.util;

import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor
public class CustomMultipartFile implements MultipartFile {
    private byte[] content;
    private String originalFilename;
    private String contentType;

    public CustomMultipartFile(byte[] content, String originalFilename, String contentType) {
        this.content = content;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return originalFilename;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        java.nio.file.Files.write(dest.toPath(), content);
    }
}
