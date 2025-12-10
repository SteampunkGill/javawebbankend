package com.milktea.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.system.PageConfigResDTO;
import com.milktea.app.dto.system.SystemConfigResDTO;
import com.milktea.app.entity.PageConfigEntity;
import com.milktea.app.entity.SystemConfigEntity;
import com.milktea.app.repository.PageConfigRepository;
import com.milktea.app.repository.SystemConfigRepository;
import com.milktea.app.service.SystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemServiceImpl implements SystemService {

    private final SystemConfigRepository systemConfigRepository;
    private final PageConfigRepository pageConfigRepository;
    private final ObjectMapper objectMapper; // For parsing JSON values in configs

    @Override
    @Transactional(readOnly = true)
    public SystemConfigResDTO getSystemConfigs() {
        SystemConfigResDTO resDTO = new SystemConfigResDTO();

        // Fetch all configs and map to DTO fields
        // This can be done by fetching all and then filtering/mapping, or by specific queries.
        // For simplicity, fetching individual keys. In production, load all into a cache.
        resDTO.setAppName(getConfigValue("appName", "温馨奶茶小屋"));
        resDTO.setVersion(getConfigValue("version", "1.0.0"));
        resDTO.setApiVersion(getConfigValue("apiVersion", "v1"));
        resDTO.setMaintenance(Boolean.valueOf(getConfigValue("maintenance", "false")));
        resDTO.setMaintenanceMessage(getConfigValue("maintenanceMessage", ""));

        // Customer Service Config
        SystemConfigResDTO.CustomerServiceConfigDTO csConfig = new SystemConfigResDTO.CustomerServiceConfigDTO();
        csConfig.setPhone(getConfigValue("customerService.phone", "400-123-4567"));
        csConfig.setWechat(getConfigValue("customerService.wechat", "milktea_cs"));
        csConfig.setWorkTime(getConfigValue("customerService.workTime", "周一至周五 9:00-18:00"));
        resDTO.setCustomerService(csConfig);

        // Points Config
        SystemConfigResDTO.PointsConfigDTO pointsConfig = new SystemConfigResDTO.PointsConfigDTO();
        pointsConfig.setRate(Integer.valueOf(getConfigValue("pointsConfig.rate", "100")));
        pointsConfig.setEarnRate(new BigDecimal(getConfigValue("pointsConfig.earnRate", "0.1")));
        pointsConfig.setMaxUseRatio(new BigDecimal(getConfigValue("pointsConfig.maxUseRatio", "0.5")));
        resDTO.setPointsConfig(pointsConfig);

        // Delivery Config
        SystemConfigResDTO.DeliveryConfigDTO deliveryConfig = new SystemConfigResDTO.DeliveryConfigDTO();
        deliveryConfig.setDefaultFee(new BigDecimal(getConfigValue("deliveryConfig.defaultFee", "3.00")));
        deliveryConfig.setFreeThreshold(new BigDecimal(getConfigValue("deliveryConfig.freeThreshold", "30.00")));
        deliveryConfig.setMaxDistance(Integer.valueOf(getConfigValue("deliveryConfig.maxDistance", "5000")));
        resDTO.setDeliveryConfig(deliveryConfig);

        // Payment Config
        SystemConfigResDTO.PaymentConfigDTO paymentConfig = new SystemConfigResDTO.PaymentConfigDTO();
        // Assume supportedMethods is stored as a JSON array string in DB
        String supportedMethodsJson = getConfigValue("paymentConfig.supportedMethods", "[\"alipay\", \"wechat\", \"balance\"]");
        try {
            // 使用 TypeReference 来指定泛型类型
            paymentConfig.setSupportedMethods(objectMapper.readValue(supportedMethodsJson, new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            log.error("Failed to parse paymentConfig.supportedMethods JSON: {}", supportedMethodsJson, e);
            paymentConfig.setSupportedMethods(Collections.emptyList());
        }
        paymentConfig.setDefaultMethod(getConfigValue("paymentConfig.defaultMethod", "alipay"));
        resDTO.setPaymentConfig(paymentConfig);

        // Notification Config
        SystemConfigResDTO.NotificationConfigDTO notificationConfig = new SystemConfigResDTO.NotificationConfigDTO();
        notificationConfig.setOrderStatus(Boolean.valueOf(getConfigValue("notificationConfig.orderStatus", "true")));
        notificationConfig.setPromotion(Boolean.valueOf(getConfigValue("notificationConfig.promotion", "true")));
        notificationConfig.setSystem(Boolean.valueOf(getConfigValue("notificationConfig.system", "true")));
        resDTO.setNotificationConfig(notificationConfig);

        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PageConfigResDTO getPageConfigs(String pageName) {
        List<PageConfigEntity> configs = pageConfigRepository.findByPageName(pageName);
        if (configs.isEmpty()) {
            // Or throw new BusinessException(ErrorCode.NOT_FOUND, "Page config not found for: " + pageName);
            return new PageConfigResDTO(pageName, Collections.emptyList());
        }

        List<PageConfigResDTO.ConfigItemDTO> configItemDTOs = configs.stream()
                .map(entity -> new PageConfigResDTO.ConfigItemDTO(entity.getKey(), entity.getValue()))
                .collect(Collectors.toList());

        return new PageConfigResDTO(pageName, configItemDTOs);
    }

    private String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByKey(key)
                .map(SystemConfigEntity::getValue)
                .orElse(defaultValue);
    }
}