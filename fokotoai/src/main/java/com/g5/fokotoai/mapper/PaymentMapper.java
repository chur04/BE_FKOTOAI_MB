package com.g5.fokotoai.mapper;

import com.g5.fokotoai.configuration.VnPayConfig;
import com.g5.fokotoai.dto.response.CreatePaymentResponse;
import com.g5.fokotoai.entity.PaymentTransaction;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.SubscriptionPackage;
import com.g5.fokotoai.enums.TransactionStatus;
import com.g5.fokotoai.service.VnPayUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentMapper {

    VnPayConfig vnPayConfig;

    public PaymentTransaction toTransactionEntity(Student student,
                                                  SubscriptionPackage pkg,
                                                  String txnRef) {
        return PaymentTransaction.builder()
                .student(student)
                .packageField(pkg)
                .vnpayTxnRef(txnRef)
                .amount(pkg.getPrice())
                .status(TransactionStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public CreatePaymentResponse toCreatePaymentResponse(PaymentTransaction transaction,
                                                         String paymentUrl) {
        return CreatePaymentResponse.builder()
                .transactionId(transaction.getId())
                .vnpayTxnRef(transaction.getVnpayTxnRef())
                .amount(transaction.getAmount())
                .paymentUrl(paymentUrl)
                .packageName(transaction.getPackageField().getPackageName())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public Map<String, String> toVnPayParams(String txnRef,
                                             SubscriptionPackage pkg,
                                             String clientIp) {
        long amountInVnPay = pkg.getPrice()
                                .multiply(BigDecimal.valueOf(100))
                                .longValue();

        String createDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                                         .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String orderInfo = "Thanh toan goi " + pkg.getPackageName();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version",    vnPayConfig.getApiVersion());
        params.put("vnp_Command",    vnPayConfig.getCommand());
        params.put("vnp_TmnCode",    vnPayConfig.getTmnCode());
        params.put("vnp_Amount",     String.valueOf(amountInVnPay));
        params.put("vnp_CurrCode",   vnPayConfig.getCurrencyCode());
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_OrderInfo",  orderInfo);
        params.put("vnp_OrderType",  vnPayConfig.getOrderType());
        params.put("vnp_Locale",     vnPayConfig.getLocale());
        params.put("vnp_ReturnUrl",  vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr",     clientIp);
        params.put("vnp_CreateDate", createDate);
        return params;
    }

    public String buildSignedPaymentUrl(Map<String, String> params) {
        String secureHash = VnPayUtil.buildSecureHash(params, vnPayConfig.getHashSecret());
        params.put("vnp_SecureHash", secureHash);
        return vnPayConfig.getPayUrl() + "?" + VnPayUtil.buildQueryString(params);
    }
}
