package com.g5.fokotoai.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeaderboardEntryDTO {
    Long studentId;
    String fullname;
    Integer rankPoints;
    Integer rankPosition;
    String avatarUrl;
}
