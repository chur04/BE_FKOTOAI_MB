package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.PackageSubStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "subscription_packages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_subscription_package_code",
                        columnNames = {"package_code"}
                )
        }
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SubscriptionPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Size(max = 50)
    @NotNull
    @Column(name = "package_code", nullable = false, length = 50)
    private String packageCode;

    @Size(max = 100)
    @NotNull
    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;

    @NotNull
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status")
    private PackageSubStatus status = PackageSubStatus.ACTIVE;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}