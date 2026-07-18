package com.g5.fokotoai.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class InkStroke {

    @NotEmpty
    List<Double> x;

    @NotEmpty
    List<Double> y;

    @NotEmpty
    List<Double> t;
}
