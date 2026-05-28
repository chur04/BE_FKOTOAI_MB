package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(
        name = "admin_audit_logs",
        indexes = {
                @Index(name = "idx_audit_admin", columnList = "admin_id"),
                @Index(name = "idx_audit_target", columnList = "target_table, target_id"),
                @Index(name = "idx_audit_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AdminAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "admin_id" , nullable = true)
    private Admin admin;

    @Size(max = 100)
    @NotNull
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Size(max = 100)
    @Column(name = "target_table", length = 100)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "detail")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> detail;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}