package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.EmailStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "email_logs")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    private Long logId;

    @Email
    @Size(max = 150)
    @NotNull
    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    @Size(max = 255)
    @NotNull
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @NotNull
    @Column(name = "body", nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmailStatus status;

    @Size(max = 50)
    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "sent_at")
    private Instant sentAt;

}