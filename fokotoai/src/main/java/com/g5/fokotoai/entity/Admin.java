package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.AdminStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "admins")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;


    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50 , unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;


    @Size(max = 150)
    @NotNull
    @Column(name = "email", nullable = false, length = 150 , unique = true)
    private String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdminStatus status = AdminStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;

    @Column(name = "updated_at")
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant updatedAt;
}