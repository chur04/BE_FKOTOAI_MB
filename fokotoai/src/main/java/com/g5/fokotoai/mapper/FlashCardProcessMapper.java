package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.response.FlashCardProgressResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FlashCardProcessMapper {

    FlashCardProgressResponse toResponse(Long totalStudied, Long totalMastered, Long totalInProgress, Double masteredPercent);
}
