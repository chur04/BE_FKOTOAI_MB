package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
@Builder

public class AuthenticateResponse {
    String token ;
    boolean isAuthenticate ;
}
