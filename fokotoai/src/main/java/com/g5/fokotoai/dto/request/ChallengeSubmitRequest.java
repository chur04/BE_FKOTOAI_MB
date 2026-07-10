package com.g5.fokotoai.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChallengeSubmitRequest {

    @NotNull(message = "sessionId must not be null")
    Long sessionId ;

    @NotEmpty(message = "answers must not be empty")
    @Valid
    List<StudentAnswer> answers ;
}
