package com.g5.fokotoai.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "RESET_TOKEN_CANNOT_EMPTY")
    String resetToken;

    @NotBlank(message = "PASSWORD_CANNOT_EMPTY")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    String newPassword;

    @NotBlank(message = "CONFIRM_PASSWORD_CANNOT_EMPTY")
    String confirmPassword;
}
