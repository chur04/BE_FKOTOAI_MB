package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.Level;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @Size(max = 100)
    @NotNull
    @Column(name = "chapter_name", nullable = false, length = 100)
    private String chapterName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private Level level;

    @NotNull
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;


    @Column(name = "description" , columnDefinition = "TEXT")
    private String description;

}