package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.AnswerOption;
import com.g5.fokotoai.enums.CommonStatus;
import com.g5.fokotoai.enums.Level;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "questions",
        indexes = {
                @Index(name = "idx_questions_category", columnList = "category_id"),
                @Index(name = "idx_questions_level",    columnList = "level")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ExamCategory category;

    @Lob
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_image_url", length = 500)
    private String questionImageUrl;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Lob
    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @Lob
    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @Lob
    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @Lob
    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_answer", nullable = false, length = 1)
    private AnswerOption correctAnswer;

    @Lob
    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 2)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "related_vocab_id")
    private Vocabulary relatedVocab;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private CommonStatus status = CommonStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private Admin createdBy;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private Instant createdAt;

    @Column(name = "updated_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private Instant updatedAt;
}