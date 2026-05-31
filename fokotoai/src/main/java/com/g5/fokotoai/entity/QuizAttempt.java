package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.PassFail;
import com.g5.fokotoai.enums.QuizAttemptType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "quiz_attempts",
        indexes = {
                @Index(name = "idx_attempts_student", columnList = "student_id"),
                @Index(name = "idx_attempts_submitted_at", columnList = "submitted_at")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "template_id")
    private ExamTemplate template;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false)
    private QuizAttemptType attemptType;

    @NotNull
    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @NotNull
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @NotNull
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @NotNull
    @Column(name = "time_taken_seconds", nullable = false)
    private Integer timeTakenSeconds;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pass_fail", nullable = false)
    private PassFail passFail;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "submitted_at")
    private Instant submittedAt;

}