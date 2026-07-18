package com.g5.fokotoai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginRequest {
    @NotBlank(message = "EMAIL_OR_USERNAME_REQUIRED")
    String usernameOrEmail;

    @NotBlank(message = "PASSWORD_REQUIRED")
    String password;
}
