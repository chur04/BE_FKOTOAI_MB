package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "chapter_name", nullable = false, length = 100)
    private String chapterName;

    @NotNull
    @Lob
    @Column(name = "level", nullable = false)
    private String level;

    @NotNull
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Lob
    @Column(name = "description")
    private String description;

}