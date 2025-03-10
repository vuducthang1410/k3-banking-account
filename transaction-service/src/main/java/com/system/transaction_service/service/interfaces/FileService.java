package com.system.transaction_service.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String upload(MultipartFile multipartFile, String fileName);

    void remove(String fileName);
}
