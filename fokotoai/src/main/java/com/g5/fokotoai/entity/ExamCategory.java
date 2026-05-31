package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.ExamCategoryStatus;
import com.g5.fokotoai.enums.ExamCategoryType;
import com.g5.fokotoai.enums.Level;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "exam_categories")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ExamCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private ExamCategoryType categoryType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id")
    private ExamCategory parent;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExamCategoryStatus status = ExamCategoryStatus.ACTIVE;

}