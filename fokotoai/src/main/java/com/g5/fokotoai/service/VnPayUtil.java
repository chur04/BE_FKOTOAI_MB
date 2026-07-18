package com.g5.fokotoai.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class VnPayUtil {

    private VnPayUtil() {
    }


    public static String buildSecureHash(Map<String, String> params, String secretKey) {
        Map<String, String> sorted = new TreeMap<>(params);

        StringBuilder queryBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isBlank()) {
                if (!queryBuilder.isEmpty()) {
                    queryBuilder.append('&');
                }
                queryBuilder.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                            .append('=')
                            .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            }
        }

        return hmacSHA512(secretKey, queryBuilder.toString());
    }

    public static String buildQueryString(Map<String, String> params) {
        Map<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                if (!sb.isEmpty()) sb.append('&');
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
                  .append('=')
                  .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexStr = new StringBuilder();
            for (byte b : hash) {
                hexStr.append(String.format("%02x", b));
            }
            return hexStr.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error computing HMAC-SHA512", e);
        }
    }
}
