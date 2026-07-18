package com.g5.fokotoai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TranslateRequest {

    @NotBlank(message = "TRANSLATE_TEXT_REQUIRED")
    String text;

    @NotBlank(message = "TRANSLATE_SOURCE_LANG_REQUIRED")
    String sourceLang;

    @NotBlank(message = "TRANSLATE_TARGET_LANG_REQUIRED")
    String targetLang;
}
