// File: milktea-backend/src/main/java/com.milktea.app/dto/file/FileUploadResDTO.java
package com.milktea.app.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResDTO {
    private String url;
    private String path;
    private Integer size;
    private String type;
    private Integer width;
    private Integer height;
}