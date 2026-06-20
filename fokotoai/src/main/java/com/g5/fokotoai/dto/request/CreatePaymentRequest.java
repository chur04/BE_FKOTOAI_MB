package com.g5.fokotoai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request body khi học sinh chọn gói Premium và yêu cầu tạo URL thanh toán.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePaymentRequest {

    @NotNull(message = "PACKAGE_NOT_FOUND")
    Long packageId;
    // String bankCode;
}
