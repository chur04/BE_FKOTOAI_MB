package com.g5.fokotoai.entity;

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

    /**
     * Quyền sở hữu linh hoạt:
     *   NULL     → Chapter Hệ thống (System/Global) do Admin tạo.
     *              Tất cả Student đều xem được, nhưng KHÔNG được sửa/xóa.
     *   NOT NULL → Chapter Cá nhân (Private) do Student đó tạo.
     *              Chỉ đúng Student sở hữu mới xem, sửa và xóa được.
     * ON DELETE CASCADE: Student bị xóa → toàn bộ Chapter riêng của họ bị xóa theo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id")
    private Student student;

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

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;
}