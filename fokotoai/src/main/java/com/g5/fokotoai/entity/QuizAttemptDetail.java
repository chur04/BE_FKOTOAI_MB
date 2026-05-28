package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.CorrectAnswer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "quiz_attempt_details")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class QuizAttemptDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

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

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

}