package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.Level;
import com.g5.fokotoai.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "students",
        indexes = {
                @Index(name = "idx_students_status",      columnList = "status"),
                @Index(name = "idx_students_rank_points", columnList = "rank_points"),
                @Index(name = "idx_students_level",       columnList = "current_level")
        })
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false, length = 2)
    private Level currentLevel;

    @Column(name = "quiz_subscription_expiry")
    private Instant quizSubscriptionExpiry;

    @ColumnDefault("0")
    @Column(name = "streak_count")
    private Integer streakCount;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @ColumnDefault("1000")
    @Column(name = "rank_points")
    private Integer rankPoints;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant createdAt;

    @Column(name = "updated_at")
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    private Instant updatedAt;
}