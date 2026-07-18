package com.g5.fokotoai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response trả về khi client lấy danh sách gói Premium.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPackageResponse {

    Long packageId;
    String packageCode;
    String packageName;
    BigDecimal price;
    Integer durationDays;
    String description;
    String status;
    Instant createdAt;
}
