// File: milktea-backend/src/main/java/com.milktea.app/controller/SystemV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.dto.system.PageConfigResDTO;
import com.milktea.app.dto.system.SystemConfigResDTO;
import com.milktea.app.service.SystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/system") // Base path for system module
@RequiredArgsConstructor
@Slf4j
public class SystemV1Controller {

    private final SystemService systemService;

    @GetMapping("/config") // Matches /system/config
    public ApiResponse<SystemConfigResDTO> getSystemConfigs() {
        log.info("Fetching system configurations.");
        SystemConfigResDTO resDTO = systemService.getSystemConfigs();
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/pageconfig/{pageName}") // Matches /system/pageconfig/{page}
    public ApiResponse<PageConfigResDTO> getPageConfigs(@PathVariable("pageName") String pageName) { // Renamed path variable
        log.info("Fetching page configurations for page: {}", pageName);
        PageConfigResDTO resDTO = systemService.getPageConfigs(pageName);
        return ApiResponse.success(resDTO);
    }
}