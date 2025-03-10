package com.system.customer_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface S3FileService {

    /**
     * Uploads a file to the Amazon S3 bucket.
     *
     * This method accepts a `MultipartFile` and uploads it to the configured S3 bucket.
     * The file will be stored with its original filename as the key in S3. After a successful upload,
     * the method returns the URL of the uploaded file.
     *
     * @param file the file to be uploaded to Amazon S3.
     * @return the URL of the uploaded file in S3.
     * @throws Exception if any error occurs during the upload process.
     * @throws IOException if there is an issue with file I/O during the upload.
     */
    String uploadFile(MultipartFile file) throws Exception, IOException;

    /**
     * Deletes a file from the Amazon S3 bucket.
     *
     * This method accepts the URL of a file and deletes it from the S3 bucket.
     * It extracts the file's key from the provided URL and uses it to identify
     * and remove the file from S3.
     *
     * @param url the URL of the file to be deleted from S3.
     * @return a confirmation message indicating whether the file was successfully deleted.
     * @throws Exception if any error occurs during the file deletion process.
     */
    void deleteFile(String url) throws Exception;

}
