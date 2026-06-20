package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.CreatePaymentRequest;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.CreatePaymentResponse;
import com.g5.fokotoai.dto.response.SubscriptionPackageResponse;
import com.g5.fokotoai.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý Use Case: Purchase Premium Subscription.
 *
 * Base URL: /FKOTOAI/payment
 *
 * Các API:
 * - GET  /packages      → Lấy danh sách gói Premium (cần X-Student-Id)
 * - POST /create-url    → Tạo URL thanh toán VNPay (cần X-Student-Id)
 * - GET  /vnpay-ipn     → Webhook VNPay gọi ngầm (KHÔNG cần header)
 * - GET  /vnpay-return  → VNPay redirect sau thanh toán (dùng để hiển thị kết quả)
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @GetMapping("/packages")
    public ApiResponse<List<SubscriptionPackageResponse>> getActivePackages(
            @RequestHeader("X-Student-Id") Long studentId) {

        return ApiResponse.<List<SubscriptionPackageResponse>>builder()
                .code(8386)
                .message("success")
                .result(paymentService.getActivePackages(studentId))
                .build();
    }

    @PostMapping("/create-url")
    public ApiResponse<CreatePaymentResponse> createPaymentUrl(
            @RequestHeader("X-Student-Id") Long studentId,
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {

        return ApiResponse.<CreatePaymentResponse>builder()
                .code(8386)
                .message("success")
                .result(paymentService.createPaymentUrl(studentId, request, httpRequest))
                .build();
    }

    @GetMapping("/vnpay-ipn")
    public Map<String, String> vnPayIpn(@RequestParam Map<String, String> params) {
        return paymentService.processIpnCallback(params);
    }

    @GetMapping("/vnpay-return")
    public ApiResponse<String> vnPayReturn(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef       = params.get("vnp_TxnRef");

        if ("00".equals(responseCode)) {
            return ApiResponse.<String>builder()
                    .code(8386)
                    .message("Payment successful")
                    .result("Transaction " + txnRef + " completed successfully")
                    .build();
        } else {
            return ApiResponse.<String>builder()
                    .code(Integer.parseInt("1" + responseCode))  // code phân biệt loại lỗi
                    .message("Payment failed or cancelled")
                    .result("Transaction " + txnRef + " failed with code: " + responseCode)
                    .build();
        }
    }
}
