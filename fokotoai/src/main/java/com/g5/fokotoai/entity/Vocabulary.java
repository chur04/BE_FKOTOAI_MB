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
@Table(name = "vocabulary")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocab_id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "word", nullable = false, length = 100)
    private String word;

    @Size(max = 100)
    @NotNull
    @Column(name = "reading", nullable = false, length = 100)
    private String reading;

    @NotNull
    @Lob
    @Column(name = "meaning", nullable = false)
    private String meaning;

    @Size(max = 50)
    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @NotNull
    @Lob
    @Column(name = "level", nullable = false)
    private String level;

    @Size(max = 500)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Lob
    @Column(name = "example_sentence")
    private String exampleSentence;

    @Lob
    @Column(name = "example_meaning")
    private String exampleMeaning;

    @Size(max = 100)
    @Column(name = "onyomi", length = 100)
    private String onyomi;

    @Size(max = 100)
    @Column(name = "kunyomi", length = 100)
    private String kunyomi;

    @Size(max = 500)
    @Column(name = "stroke_order_url", length = 500)
    private String strokeOrderUrl;

    @ColumnDefault("0")
    @Column(name = "is_kanji")
    private Boolean isKanji;

    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private Admin createdBy;

    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "created_at")
    private Instant createdAt;

}