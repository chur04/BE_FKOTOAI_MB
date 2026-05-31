package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class StudentProfileResponse {
    String fullname;
    String avatarUrl;
    Integer streakCount;
    Integer rankPoints;
}
