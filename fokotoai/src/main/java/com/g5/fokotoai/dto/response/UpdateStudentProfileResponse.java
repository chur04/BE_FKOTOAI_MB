package com.g5.fokotoai.dto.response;

import com.g5.fokotoai.enums.Level;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStudentProfileResponse {

    Long studentId;
    String fullname;
    String username;
    String email;
    String avatarUrl;
    Level currentLevel;
    Integer streakCount;
    Integer rankPoints;
    boolean hasPremium;
    Instant quizSubscriptionExpiry;
    String currentPackageName;
}
