package com.g5.fokotoai.entity;

import com.g5.fokotoai.enums.Level;
import com.g5.fokotoai.enums.StudentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    Long studentId;

    @Column(name = "fullname", nullable = false, length = 100)
    String fullname;

    @Column(name = "username", nullable = false, length = 50 , unique = true)
    String username;

    @Column(name = "password_hash", nullable = false)
    String passwordHash;

    @Column(name = "email", nullable = false, length = 150 , unique = true)
    String email;

    @Column(name = "avatar_url", length = 500)
    String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false)
    Level currentLevel;

    @Column(name = "quiz_subscription_expiry")
    Instant quizSubscriptionExpiry;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "streak_count")
    Integer streakCount = 0;

    @Column(name = "last_login_date")
    LocalDate lastLoginDate;

    @Builder.Default
    @ColumnDefault("1000")
    @Column(name = "rank_points")
    Integer rankPoints = 1000;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    StudentStatus status = StudentStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

}