package com.g5.fokotoai.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentProfileDTO {
    String fullname;
    String avatarUrl;
    Integer streakCount;
    Integer rankPoints;
}
