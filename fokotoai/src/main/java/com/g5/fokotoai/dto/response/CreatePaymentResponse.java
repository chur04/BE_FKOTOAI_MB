package com.g5.fokotoai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePaymentResponse {

    Long transactionId;
    String vnpayTxnRef;
    BigDecimal amount;
    String paymentUrl;
    String packageName;
    Instant createdAt;
}
