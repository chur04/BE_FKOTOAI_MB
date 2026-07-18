package com.g5.fokotoai.service;

import com.g5.fokotoai.configuration.VnPayConfig;
import com.g5.fokotoai.dto.request.CreatePaymentRequest;
import com.g5.fokotoai.dto.response.CreatePaymentResponse;
import com.g5.fokotoai.dto.response.SubscriptionPackageResponse;
import com.g5.fokotoai.entity.PaymentTransaction;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.SubscriptionPackage;
import com.g5.fokotoai.enums.PackageSubStatus;
import com.g5.fokotoai.enums.TransactionStatus;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.mapper.PaymentMapper;
import com.g5.fokotoai.mapper.SubscriptionPackageMapper;
import com.g5.fokotoai.repository.PaymentTransactionRepository;
import com.g5.fokotoai.repository.StudentRepository;
import com.g5.fokotoai.repository.SubscriptionPackageRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    SubscriptionPackageRepository packageRepository;
    PaymentTransactionRepository  transactionRepository;
    StudentRepository             studentRepository;
    SubscriptionPackageMapper     packageMapper;
    PaymentMapper                 paymentMapper;
    VnPayConfig                   vnPayConfig;


    @Transactional(readOnly = true)
    public List<SubscriptionPackageResponse> getActivePackages(Long studentId) {
        findStudentOrThrow(studentId);

        return packageRepository.findByStatus(PackageSubStatus.ACTIVE)
                .stream()
                .map(packageMapper::toResponse)
                .toList();
    }


    @Transactional
    public CreatePaymentResponse createPaymentUrl(Long studentId,
                                                  CreatePaymentRequest request,
                                                  HttpServletRequest httpRequest) {
        Student student = findStudentOrThrow(studentId);

        SubscriptionPackage pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        if (pkg.getStatus() != PackageSubStatus.ACTIVE) {
            throw new AppException(ErrorCode.PACKAGE_INACTIVE);
        }

        // Chặn mua mới khi đang còn gói hạn
        Instant currentExpiry = student.getQuizSubscriptionExpiry();
        if (currentExpiry != null && currentExpiry.isAfter(Instant.now())) {
            throw new AppException(ErrorCode.SUBSCRIPTION_STILL_ACTIVE);
        }

        String txnRef = generateUniqueTxnRef();

        PaymentTransaction transaction = paymentMapper.toTransactionEntity(student, pkg, txnRef);
        transaction = transactionRepository.save(transaction);

        String clientIp  = getClientIp(httpRequest);
        Map<String, String> vnpParams = buildVnPayParams(txnRef, pkg, clientIp);
        String paymentUrl = buildSignedPaymentUrl(vnpParams);

        log.info("[Payment] Created transaction txnRef={} for studentId={} packageId={}",
                txnRef, studentId, pkg.getPackageId());

        return paymentMapper.toCreatePaymentResponse(transaction, paymentUrl);
    }

    @Transactional
    public Map<String, String> processIpnCallback(Map<String, String> params) {

        String receivedHash = params.get("vnp_SecureHash");
        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        String computedHash = VnPayUtil.buildSecureHash(verifyParams, vnPayConfig.getHashSecret());
        if (!computedHash.equalsIgnoreCase(receivedHash)) {
            log.warn("[IPN] Invalid signature. txnRef={}", params.get("vnp_TxnRef"));
            return vnPayIpnResponse("97", "Invalid Checksum");
        }

        String txnRef = params.get("vnp_TxnRef");
        PaymentTransaction transaction = transactionRepository.findByVnpayTxnRef(txnRef)
                .orElse(null);

        if (transaction == null) {
            log.warn("[IPN] Transaction not found. txnRef={}", txnRef);
            return vnPayIpnResponse("01", "Order not found");
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.info("[IPN] Transaction already processed. txnRef={} status={}",
                    txnRef, transaction.getStatus());
            return vnPayIpnResponse("02", "Order already confirmed");
        }

        long vnpAmountRaw = Long.parseLong(params.get("vnp_Amount"));
        BigDecimal vnpAmount = BigDecimal.valueOf(vnpAmountRaw).divide(BigDecimal.valueOf(100));

        if (transaction.getAmount().compareTo(vnpAmount) != 0) {
            log.warn("[IPN] Amount mismatch. txnRef={} expected={} received={}",
                    txnRef, transaction.getAmount(), vnpAmount);
            return vnPayIpnResponse("04", "Invalid amount");
        }

        String responseCode = params.get("vnp_ResponseCode");
        String vnpTxnNo     = params.get("vnp_TransactionNo");

        if ("00".equals(responseCode)) {
            handleSuccessfulPayment(transaction, vnpTxnNo, params.get("vnp_BankCode"));
            return vnPayIpnResponse("00", "Confirm Success");
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setVnpayResponseCode(responseCode);
            transaction.setVnpayTxnNo(vnpTxnNo);
            transaction.setWebhookReceivedAt(Instant.now());
            transactionRepository.save(transaction);

            log.info("[IPN] Payment failed. txnRef={} responseCode={}", txnRef, responseCode);
            return vnPayIpnResponse("00", "Confirm Success"); // VNPay vẫn cần "00" để xác nhận IPN đã nhận
        }
    }

    private void handleSuccessfulPayment(PaymentTransaction transaction,
                                         String vnpTxnNo,
                                         String bankCode) {
        Instant now = Instant.now();
        SubscriptionPackage pkg = transaction.getPackageField();
        int durationDays = pkg.getDurationDays();

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setVnpayTxnNo(vnpTxnNo);
        transaction.setVnpayResponseCode("00");
        transaction.setPaymentMethod(bankCode);
        transaction.setActivatedAt(now);
        transaction.setExpiresAt(now.plusSeconds((long) durationDays * 86400));
        transaction.setWebhookReceivedAt(now);
        transactionRepository.save(transaction);

        Student student = transaction.getStudent();
        // Không cộng dồn – luôn tính từ thời điểm kích hoạt
        Instant newExpiry = now.plusSeconds((long) durationDays * 86400);

        student.setQuizSubscriptionExpiry(newExpiry);
        studentRepository.save(student);

        log.info("[IPN] Payment SUCCESS. studentId={} txnRef={} newExpiry={}",
                student.getStudentId(), transaction.getVnpayTxnRef(), newExpiry);
    }


    private String generateUniqueTxnRef() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(fmt);
        String uuid8     = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String txnRef    = timestamp + uuid8;

        if (transactionRepository.existsByVnpayTxnRef(txnRef)) {
            txnRef = timestamp + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
        return txnRef;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }

    private Map<String, String> vnPayIpnResponse(String rspCode, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", rspCode);
        response.put("Message", message);
        return response;
    }

    private Map<String, String> buildVnPayParams(String txnRef,
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

    private String buildSignedPaymentUrl(Map<String, String> params) {
        String secureHash = VnPayUtil.buildSecureHash(params, vnPayConfig.getHashSecret());
        params.put("vnp_SecureHash", secureHash);
        return vnPayConfig.getPayUrl() + "?" + VnPayUtil.buildQueryString(params);
    }
}
