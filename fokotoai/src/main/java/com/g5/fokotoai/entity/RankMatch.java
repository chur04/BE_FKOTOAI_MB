package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.Level;
import com.g5.fokotoai.enums.MatchStatus;
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
@Table(name = "rank_matches",
        indexes = {
                @Index(name = "idx_matches_player1", columnList = "player1_id"),
                @Index(name = "idx_matches_player2", columnList = "player2_id"),
                @Index(name = "idx_matches_status",  columnList = "status")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RankMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "player1_id", nullable = false)
    private Student player1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "player2_id", nullable = false)
    private Student player2;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 2)
    private Level level;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 12)
    private MatchStatus status = MatchStatus.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "winner_id")
    private Student winner;

    @ColumnDefault("0")
    @Column(name = "player1_score")
    private Integer player1Score;

    @ColumnDefault("0")
    @Column(name = "player2_score")
    private Integer player2Score;

    @Column(name = "player1_time_ms")
    private Long player1TimeMs;

    @Column(name = "player2_time_ms")
    private Long player2TimeMs;

    @Column(name = "player1_elo_change")
    private Integer player1EloChange;

    @Column(name = "player2_elo_change")
    private Integer player2EloChange;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private Instant createdAt;
}