package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_word_metrics")
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

    @ColumnDefault("(`quiz_incorrect` + `flashcard_incorrect`)")
    @Column(name = "total_wrong_count")
    private Integer totalWrongCount;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}