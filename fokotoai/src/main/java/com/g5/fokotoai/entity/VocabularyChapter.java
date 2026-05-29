package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.Level;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "vocabulary_chapters")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VocabularyChapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(name = "chapter_name", nullable = false, length = 100)
    private String chapterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 2)
    private Level level;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}