// File: milktea-backend/src/main/java/com.milktea.app/service/impl/FileServiceImpl.java
package com.milktea.app.service.impl;

import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.file.FileUploadResDTO;
import com.milktea.app.entity.FileEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.repository.FileRepository;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir:uploads}") // Configurable upload directory
    private String uploadDir;
    @Value("${file.max-size-mb:5}") // Max file size in MB
    private long maxFileSizeMb;
    @Value("${file.supported-image-types:image/jpeg,image/png,image/gif}")
    private List<String> supportedImageTypes;

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FileUploadResDTO uploadFile(Long userId, MultipartFile file, String category) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "File cannot be empty.");
        }

        if (file.getSize() > maxFileSizeMb * 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, "File size exceeds limit (" + maxFileSizeMb + "MB).");
        }

        String contentType = file.getContentType();
        if (contentType == null || !supportedImageTypes.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, "Unsupported file type: " + contentType + ". Only JPEG, PNG, GIF are allowed.");
        }

        try {
            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath);

            // Get image dimensions
            BufferedImage bimg = ImageIO.read(file.getInputStream());
            int width = bimg.getWidth();
            int height = bimg.getHeight();

            UserEntity user = null;
            if (userId != null) {
                user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
            }

            FileEntity fileEntity = new FileEntity();
            fileEntity.setUser(user);
            fileEntity.setUrl("/uploads/" + fileName); // Assuming a /uploads endpoint exposes these files
            fileEntity.setPath(filePath.toString());
            fileEntity.setSize((int) file.getSize());
            fileEntity.setType(getFileTypeFromContentType(contentType));
            fileEntity.setCategory(category);
            fileEntity.setWidth(width);
            fileEntity.setHeight(height);
            fileEntity.setMimeType(contentType);
            fileEntity.setCreatedAt(Instant.now());

            fileEntity = fileRepository.save(fileEntity);

            return new FileUploadResDTO(
                    fileEntity.getUrl(),
                    fileEntity.getPath(),
                    fileEntity.getSize(),
                    fileEntity.getMimeType(),
                    fileEntity.getWidth(),
                    fileEntity.getHeight()
            );

        } catch (IOException e) {
            log.error("File upload failed for user {}: {}", userId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "Failed to upload file: " + e.getMessage());
        }
    }

    private String getFileTypeFromContentType(String contentType) {
        if (contentType == null) return "unknown";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        if (contentType.startsWith("audio/")) return "audio";
        return "other";
    }
}