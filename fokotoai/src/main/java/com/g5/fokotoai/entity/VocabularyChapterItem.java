package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "vocabulary_chapter_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_vci_chapter_vocab",
                        columnNames = {"chapter_id", "vocab_id"})
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VocabularyChapterItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chapter_id", nullable = false)
    private VocabularyChapter chapter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "vocab_id", nullable = false)
    private Vocabulary vocab;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}