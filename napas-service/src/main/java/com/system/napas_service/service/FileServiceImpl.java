package com.system.napas_service.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.system.napas_service.service.interfaces.FileService;
import com.system.napas_service.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${firebase.image.base.url}")
    private String imageBaseUrl;

    @Override
    public String upload(MultipartFile multipartFile, String fileName) {

        try {

            if (!multipartFile.isEmpty()) {

                InputStream inputStream = multipartFile.getInputStream();
                Bucket bucket = StorageClient.getInstance().bucket();
                bucket.create(fileName, inputStream, "image/" +
                        this.getExtension(Objects.requireNonNull(multipartFile.getOriginalFilename())));

                return String.format(imageBaseUrl, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            }
        } catch (Exception ignore) {

            log.info("Upload file name {} failed", fileName);
        }

        return Constant.BLANK;
    }


    @Override
    public void remove(String fileName) {

        try {

            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.get(fileName).delete();
        } catch (Exception ignore) {

            log.info("Remove file name {} failed", fileName);
        }
    }

    private String getExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {

            return fileName.substring(dotIndex + 1);
        }

        return "png";
    }
}
