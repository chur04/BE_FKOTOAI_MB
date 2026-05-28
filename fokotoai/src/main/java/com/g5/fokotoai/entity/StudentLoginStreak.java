package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "student_login_streaks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_student_login_date",
                        columnNames = {"student_id", "login_date"}
                )
        },
        indexes = {
                @Index(name = "idx_login_streak_student", columnList = "student_id"),
                @Index(name = "idx_login_streak_date", columnList = "login_date")
        }
)

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class StudentLoginStreak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Column(name = "login_date", nullable = false)
    private LocalDate loginDate;

}