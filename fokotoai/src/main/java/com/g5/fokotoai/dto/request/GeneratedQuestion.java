package com.g5.fokotoai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestion {
    private String question;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
}