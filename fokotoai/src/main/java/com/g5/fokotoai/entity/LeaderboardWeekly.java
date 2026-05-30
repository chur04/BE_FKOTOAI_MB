package com.g5.fokotoai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "leaderboard_weekly",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_leaderboard_student_week",
                        columnNames = {"student_id", "week_start"})
        },
        indexes = {
                @Index(name = "idx_leaderboard_week_points",
                        columnList = "week_start, weekly_points")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LeaderboardWeekly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @ColumnDefault("0")
    @Column(name = "weekly_points")
    private Integer weeklyPoints;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @ColumnDefault("0")
    @Column(name = "matches_played")
    private Integer matchesPlayed;

    @ColumnDefault("0")
    @Column(name = "matches_won")
    private Integer matchesWon;
}