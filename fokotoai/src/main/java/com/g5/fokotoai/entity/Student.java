package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.JapaneseLevel;
import com.g5.fokotoai.enums.StudentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "students",
        indexes = {
                @Index(name = "idx_students_status", columnList = "status"),
                @Index(name = "idx_students_rank_points", columnList = "rank_points"),
                @Index(name = "idx_students_level", columnList = "current_level")
        }
)

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50 , unique = true)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 150)
    @NotNull
    @Column(name = "email", nullable = false, length = 150 , unique = true)
    private String email;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false)
    private JapaneseLevel currentLevel;

    @Column(name = "quiz_subscription_expiry")
    private Instant quizSubscriptionExpiry;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "streak_count")
    private Integer streakCount = 0;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Builder.Default
    @ColumnDefault("1000")
    @Column(name = "rank_points")
    private Integer rankPoints = 1000;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StudentStatus status = StudentStatus.ACTIVE;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}