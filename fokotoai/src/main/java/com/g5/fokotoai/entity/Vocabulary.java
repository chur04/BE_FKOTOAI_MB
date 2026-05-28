package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.JapaneseLevel;
import com.g5.fokotoai.enums.VocabularyStatus;
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
@Table(
        name = "vocabulary",
        indexes = {
                @Index(name = "idx_vocab_level", columnList = "level"),
                @Index(name = "idx_vocab_word", columnList = "word"),
                @Index(name = "idx_vocab_is_kanji", columnList = "is_kanji")
        }
)
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
    @Column(name = "meaning", nullable = false , columnDefinition = "TEXT")
    private String meaning;

    @Size(max = 50)
    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private JapaneseLevel level;

    @Size(max = 500)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;


    @Column(name = "example_sentence" , columnDefinition = "TEXT")
    private String exampleSentence;


    @Column(name = "example_meaning" , columnDefinition = "TEXT")
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

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_kanji")
    private Boolean isKanji = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VocabularyStatus status = VocabularyStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private Admin createdBy;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}