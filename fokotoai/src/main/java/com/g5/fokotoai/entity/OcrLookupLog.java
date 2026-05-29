package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.OcrInputType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Bảng ocr_lookup_logs
 * Cột matched_vocab_ids là JSON — ánh xạ thành String. Parse thành List/Map ở tầng Service nếu cần.
 */
@Getter
@Setter
@Entity
@Table(name = "ocr_lookup_logs")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OcrLookupLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 11)
    private OcrInputType inputType;

    @Lob
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "matched_vocab_ids", columnDefinition = "JSON")
    private String matchedVocabIds;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;
}