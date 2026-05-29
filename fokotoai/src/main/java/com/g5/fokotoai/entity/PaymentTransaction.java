package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment_transactions",
        indexes = {
                @Index(name = "idx_transactions_student",    columnList = "student_id"),
                @Index(name = "idx_transactions_status",     columnList = "status"),
                @Index(name = "idx_transactions_created_at", columnList = "created_at")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private SubscriptionPackage subscriptionPackage;

    @Column(name = "vnpay_txn_ref", nullable = false, length = 100, unique = true)
    private String vnpayTxnRef;

    @Column(name = "vnpay_txn_no", length = 100)
    private String vnpayTxnNo;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "vnpay_response_code", length = 10)
    private String vnpayResponseCode;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "webhook_received_at")
    private Instant webhookReceivedAt;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;
}