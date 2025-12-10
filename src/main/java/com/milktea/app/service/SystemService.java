// File: milktea-backend/src/main/java/com.milktea.app/service/SystemService.java
package com.milktea.app.service;

import com.milktea.app.dto.system.PageConfigResDTO;
import com.milktea.app.dto.system.SystemConfigResDTO;

import java.util.List;

public interface SystemService {
    SystemConfigResDTO getSystemConfigs();
    PageConfigResDTO getPageConfigs(String pageName);
}