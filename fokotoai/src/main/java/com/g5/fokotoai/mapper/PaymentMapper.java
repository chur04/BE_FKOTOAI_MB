package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.response.CreatePaymentResponse;
import com.g5.fokotoai.entity.PaymentTransaction;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.SubscriptionPackage;
import com.g5.fokotoai.enums.TransactionStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {TransactionStatus.class, Instant.class})
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", source = "student")
    @Mapping(target = "packageField", source = "pkg")
    @Mapping(target = "vnpayTxnRef", source = "txnRef")
    @Mapping(target = "amount", expression = "java(pkg.getPrice())")
    @Mapping(target = "status", expression = "java(TransactionStatus.PENDING)")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "vnpayTxnNo", ignore = true)
    @Mapping(target = "vnpayResponseCode", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "activatedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "webhookReceivedAt", ignore = true)
    PaymentTransaction toTransactionEntity(Student student, SubscriptionPackage pkg, String txnRef);

    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "vnpayTxnRef", source = "transaction.vnpayTxnRef")
    @Mapping(target = "amount", source = "transaction.amount")
    @Mapping(target = "paymentUrl", source = "paymentUrl")
    @Mapping(target = "packageName", expression = "java(transaction.getPackageField().getPackageName())")
    @Mapping(target = "createdAt", source = "transaction.createdAt")
    CreatePaymentResponse toCreatePaymentResponse(PaymentTransaction transaction, String paymentUrl);
}
