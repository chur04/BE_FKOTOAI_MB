package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(
        name = "payment_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payment_vnpay_txn_ref",
                        columnNames = {"vnpay_txn_ref"}
                )
        },
        indexes = {
                @Index(name = "idx_transactions_student", columnList = "student_id"),
                @Index(name = "idx_transactions_status", columnList = "status"),
                @Index(name = "idx_transactions_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private SubscriptionPackage packageField;

    @Size(max = 100)
    @NotNull
    @Column(name = "vnpay_txn_ref", nullable = false, length = 100)
    private String vnpayTxnRef;

    @Size(max = 100)
    @Column(name = "vnpay_txn_no", length = 100)
    private String vnpayTxnNo;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "status")
    private TransactionStatus status = TransactionStatus.PENDING;

    @Size(max = 10)
    @Column(name = "vnpay_response_code", length = 10)
    private String vnpayResponseCode;

    @Size(max = 50)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "webhook_received_at")
    private Instant webhookReceivedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}