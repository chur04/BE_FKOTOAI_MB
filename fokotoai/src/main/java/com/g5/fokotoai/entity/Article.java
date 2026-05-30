package com.g5.fokotoai.entity;

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
@Table(name = "articles",
        indexes = {
                @Index(name = "idx_articles_level",  columnList = "level"),
                @Index(name = "idx_articles_status", columnList = "status")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "content_tokenized", columnDefinition = "JSON")
    private String contentTokenized;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 2)
    private Level level;

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private CommonStatus status = CommonStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private Admin createdBy;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;

    @Column(name = "updated_at")
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant updatedAt;
}