package com.g5.fokotoai.dto.response;

import com.g5.fokotoai.enums.JapaneseLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class StudentResponse {

    String fullname ;
    String username ;
    String email ;
    String avatarUrl;
    JapaneseLevel currentLevel ;
    Instant quizSubscriptionExpiry ;
    Integer streakCount ;
    LocalDate lastLoginDate;
    Integer rankPoints ;
    Instant createdAt ;
    Instant updatedAt;

}
