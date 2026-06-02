package com.g5.fokotoai.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class GeneratedQuestion {
    String question;
    List<String> options;
    String correctAnswer;
    String explanation;
}