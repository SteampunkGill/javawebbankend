// File: milktea-backend/src/main/java/com.milktea.app/dto/system/SystemConfigResDTO.java
package com.milktea.app.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResDTO {
    private String appName;
    private String version;
    private String apiVersion;
    private Boolean maintenance;
    private String maintenanceMessage;
    private CustomerServiceConfigDTO customerService;
    private PointsConfigDTO pointsConfig;
    private DeliveryConfigDTO deliveryConfig;
    private PaymentConfigDTO paymentConfig;
    private NotificationConfigDTO notificationConfig;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerServiceConfigDTO {
        private String phone;
        private String wechat;
        private String workTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointsConfigDTO {
        private Integer rate;
        private BigDecimal earnRate;
        private BigDecimal maxUseRatio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryConfigDTO {
        private BigDecimal defaultFee;
        private BigDecimal freeThreshold;
        private Integer maxDistance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentConfigDTO {
        private List<String> supportedMethods;
        private String defaultMethod;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationConfigDTO {
        private Boolean orderStatus;
        private Boolean promotion;
        private Boolean system;
    }
}