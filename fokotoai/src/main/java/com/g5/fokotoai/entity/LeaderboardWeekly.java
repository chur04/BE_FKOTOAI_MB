package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "leaderboard_weekly",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_leaderboard_student_week",
                        columnNames = {"student_id", "week_start"}
                )
        },
        indexes = {
                @Index(name = "idx_leaderboard_week_points", columnList = "week_start, weekly_points DESC")
        }
)

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LeaderboardWeekly {
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
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @NotNull
    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "weekly_points")
    private Integer weeklyPoints = 0;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "matches_played")
    private Integer matchesPlayed = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "matches_won")
    private Integer matchesWon = 0;

}