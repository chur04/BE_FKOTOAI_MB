package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "user_word_metrics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_uwm_student_vocab",
                        columnNames = {"student_id", "vocab_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_uwm_student_wrong",
                        columnList = "student_id, total_wrong_count DESC"
                )
        }
)

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserWordMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "vocab_id", nullable = false)
    private Vocabulary vocab;

    @ColumnDefault("0")
    @Column(name = "mastered")
    private Boolean mastered;

    // Tất cả các field có DEFAULT 0 nên thêm @Builder.Default
    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "flashcard_correct")
    private Integer flashcardCorrect = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "flashcard_incorrect")
    private Integer flashcardIncorrect = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "quiz_correct")
    private Integer quizCorrect = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "quiz_incorrect")
    private Integer quizIncorrect = 0;


    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "total_wrong_count", insertable = false, updatable = false)
    private Integer totalWrongCount;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}