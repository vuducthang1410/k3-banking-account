package com.example.reporting_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
public class FilebaseStorageService { // Sửa lại tên class cho đúng

    private final S3Client s3Client;

    @Value("${filebase.s3.bucketName}")
    private String bucketName;

    // Đổi tên constructor cho khớp với class
    public FilebaseStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(byte[] fileBytes, String fileName) {
        try {
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

            return String.format("https://s3.filebase.com/%s/%s", bucketName, uniqueFileName);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload file lên Filebase S3: " + e.getMessage());
        }
    }
}
