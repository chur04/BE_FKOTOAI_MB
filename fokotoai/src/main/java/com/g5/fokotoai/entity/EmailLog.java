package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.EmailStatus;
import jakarta.persistence.*;
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

    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 6)
    private EmailStatus status = EmailStatus.SENT;

    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "sent_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant sentAt;
}