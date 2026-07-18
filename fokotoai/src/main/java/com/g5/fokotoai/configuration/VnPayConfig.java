package com.g5.fokotoai.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Bind toàn bộ config VNPay từ application.yaml (prefix: vnpay).
 * TODO: Điền tmn-code và hash-secret thật vào application.yaml trước khi deploy.
 */
@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VnPayConfig {

    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String apiVersion;
    private String command;
    private String locale;
    private String currencyCode;
    private String orderType;
}
