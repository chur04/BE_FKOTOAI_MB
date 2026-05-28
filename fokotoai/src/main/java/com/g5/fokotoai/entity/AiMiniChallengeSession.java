package com.g5.fokotoai.entity;

import com.g5.fokotoai.dto.request.GeneratedQuestion;
import com.g5.fokotoai.dto.request.WeakVocabItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(
        name = "ai_mini_challenge_sessions",
        indexes = {
                @Index(name = "idx_ai_challenge_student", columnList = "student_id"),
                @Index(name = "idx_ai_challenge_attempt", columnList = "attempt_id"),
                @Index(name = "idx_ai_challenge_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AiMiniChallengeSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Column(name = "weak_vocab_snapshot", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<WeakVocabItem> weakVocabSnapshot;

    @NotNull
    @Column(name = "generated_questions", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<GeneratedQuestion> generatedQuestions;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "attempt_id" , nullable = true)
    private QuizAttempt attempt;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}