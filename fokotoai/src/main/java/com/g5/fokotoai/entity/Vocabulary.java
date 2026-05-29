package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.VocabularyStatus;
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
@Table(name = "vocabulary",
        indexes = {
                @Index(name = "idx_vocab_level",    columnList = "level"),
                @Index(name = "idx_vocab_word",     columnList = "word"),
                @Index(name = "idx_vocab_is_kanji", columnList = "is_kanji")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocab_id", nullable = false)
    private Long vocabId;

    @Column(name = "word", nullable = false, length = 100)
    private String word;

    @Column(name = "reading", nullable = false, length = 100)
    private String reading;

    @Lob
    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 2)
    private Level level;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Lob
    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Lob
    @Column(name = "example_meaning", columnDefinition = "TEXT")
    private String exampleMeaning;

    @Column(name = "onyomi", length = 100)
    private String onyomi;

    @Column(name = "kunyomi", length = 100)
    private String kunyomi;

    @Column(name = "stroke_order_url", length = 500)
    private String strokeOrderUrl;

    @ColumnDefault("0")
    @Column(name = "is_kanji")
    private Boolean isKanji;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private VocabularyStatus status = VocabularyStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private Admin createdBy;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;
}