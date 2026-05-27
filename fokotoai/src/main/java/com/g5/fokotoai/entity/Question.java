package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "questions")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ExamCategory category;

    @NotNull
    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Size(max = 500)
    @Column(name = "question_image_url", length = 500)
    private String questionImageUrl;

    @Size(max = 500)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @NotNull
    @Lob
    @Column(name = "option_a", nullable = false)
    private String optionA;

    @NotNull
    @Lob
    @Column(name = "option_b", nullable = false)
    private String optionB;

    @NotNull
    @Lob
    @Column(name = "option_c", nullable = false)
    private String optionC;

    @NotNull
    @Lob
    @Column(name = "option_d", nullable = false)
    private String optionD;

    @NotNull
    @Lob
    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @NotNull
    @Lob
    @Column(name = "explanation", nullable = false)
    private String explanation;

    @NotNull
    @Lob
    @Column(name = "level", nullable = false)
    private String level;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "related_vocab_id")
    private Vocabulary relatedVocab;

    @ColumnDefault("'ACTIVE'")
    @Lob
    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private Admin createdBy;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}