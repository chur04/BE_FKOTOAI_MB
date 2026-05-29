package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "admin_audit_logs")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id", nullable = false)
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "target_table", length = 100)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "detail", columnDefinition = "JSON")
    private String detail;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;
}