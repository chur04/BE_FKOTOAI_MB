package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "articles")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "content_tokenized")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> contentTokenized;

    @NotNull
    @Lob
    @Column(name = "level", nullable = false)
    private String level;

    @Size(max = 255)
    @Column(name = "source")
    private String source;

    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

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