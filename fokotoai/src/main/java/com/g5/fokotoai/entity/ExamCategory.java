package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.CategoryType;
import com.g5.fokotoai.enums.CommonStatus;
import com.g5.fokotoai.enums.Level;
import jakarta.persistence.*;
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
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 10)
    private CategoryType categoryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 2)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id")
    private ExamCategory parent;

    @ColumnDefault("0")
    @Column(name = "order_index")
    private Integer orderIndex;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private CommonStatus status = CommonStatus.ACTIVE;
}