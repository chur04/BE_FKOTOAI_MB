package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Bảng user_word_metrics
 * Lưu ý: cột total_wrong_count là GENERATED ALWAYS AS (quiz_incorrect + flashcard_incorrect) STORED
 * => insertable=false, updatable=false để Hibernate không ghi vào cột này.
 */
@Getter
@Setter
@Entity
@Table(name = "user_word_metrics",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_uwm_student_vocab",
                        columnNames = {"student_id", "vocab_id"})
        },
        indexes = {
                @Index(name = "idx_uwm_student_wrong",
                        columnList = "student_id, total_wrong_count")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserWordMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id", nullable = false)
    private Long metricId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "vocab_id", nullable = false)
    private Vocabulary vocab;

    @ColumnDefault("0")
    @Column(name = "mastered")
    private Boolean mastered;

    @ColumnDefault("0")
    @Column(name = "flashcard_correct")
    private Integer flashcardCorrect;

    @ColumnDefault("0")
    @Column(name = "flashcard_incorrect")
    private Integer flashcardIncorrect;

    @ColumnDefault("0")
    @Column(name = "quiz_correct")
    private Integer quizCorrect;

    @ColumnDefault("0")
    @Column(name = "quiz_incorrect")
    private Integer quizIncorrect;

    /**
     * Cột GENERATED ALWAYS AS (quiz_incorrect + flashcard_incorrect) STORED
     * => Chỉ đọc, không được phép INSERT hoặc UPDATE từ phía Java.
     */
    @Column(name = "total_wrong_count", insertable = false, updatable = false)
    private Integer totalWrongCount;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "updated_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private Instant updatedAt;
}