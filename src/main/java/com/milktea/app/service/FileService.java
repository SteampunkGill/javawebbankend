// File: milktea-backend/src/main/java/com.milktea.app/service/FileService.java
package com.milktea.app.service;

import com.milktea.app.dto.file.FileUploadResDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResDTO uploadFile(Long userId, MultipartFile file, String category);
}