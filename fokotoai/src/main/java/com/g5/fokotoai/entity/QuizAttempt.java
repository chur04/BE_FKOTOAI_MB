package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.AttemptType;
import com.g5.fokotoai.enums.PassFail;
import jakarta.persistence.*;
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
@Table(name = "quiz_attempts",
        indexes = {
                @Index(name = "idx_attempts_student",      columnList = "student_id"),
                @Index(name = "idx_attempts_submitted_at", columnList = "submitted_at")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "template_id")
    private ExamTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false, length = 12)
    private AttemptType attemptType;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "time_taken_seconds", nullable = false)
    private Integer timeTakenSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_fail", nullable = false, length = 4)
    private PassFail passFail;

    @Column(name = "submitted_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private Instant submittedAt;
}