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
@Table(
        name = "ai_analysis_logs",
        indexes = {
                @Index(name = "idx_ai_logs_student", columnList = "student_id"),
                @Index(name = "idx_ai_logs_article", columnList = "article_id"),
                @Index(name = "idx_ai_logs_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AiAnalysisLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "article_id" , nullable = true)
    private Article article;

    @NotNull
    @Column(name = "selected_text", nullable = false, columnDefinition = "TEXT")
    private String selectedText;

    @NotNull
    @Column(name = "ai_response", nullable = false, columnDefinition = "LONGTEXT")
    private String aiResponse;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}