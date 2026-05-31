package com.g5.fokotoai.dto.request;

import com.g5.fokotoai.enums.Level;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;



@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class StudentCreateRequest {

    @NotBlank(message = "FULLNAME_REQUIRED")
    @Size(min = 5, max = 100, message = "FULLNAME_INVALID")
    String fullname;

    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, max = 50, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 100, message = "PASSWORD_INVALID")
    String passwordHash;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    @Size(max = 150)
    String email;

    @Size(max = 500)
    String avatarUrl;

    @NotNull(message = "LEVEL_REQUIRED")
    Level currentLevel;

}
