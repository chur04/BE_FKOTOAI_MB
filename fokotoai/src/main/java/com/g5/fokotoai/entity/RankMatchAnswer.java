package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.CorrectAnswer;
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
@Table(
        name = "rank_match_answers",
        indexes = {
                @Index(name = "idx_rma_match_student", columnList = "match_id, student_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RankMatchAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id", nullable = false)
    private Long answerId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "match_id", nullable = false)
    private RankMatch match;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_option")
    private CorrectAnswer selectedOption;

    @Builder.Default
    @ColumnDefault("false")
    @NotNull
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @NotNull
    @Column(name = "response_time_ms", nullable = false)
    private Integer responseTimeMs;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "answered_at")
    private Instant answeredAt;

}