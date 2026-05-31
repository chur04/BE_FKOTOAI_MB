package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class LeaderboardEntryResponse {
    Long studentId;
    String fullname;
    Integer rankPoints;
    Integer rankPosition;
    String avatarUrl;
}
