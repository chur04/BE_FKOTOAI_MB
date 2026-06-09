package com.g5.fokotoai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticateRequest {
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    @Size(max = 150)
    String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 1, max = 100, message = "PASSWORD_INVALID")
    String passwordHash;
}
