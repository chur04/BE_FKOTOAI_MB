package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.VocabularyChapterResponse;
import com.g5.fokotoai.entity.VocabularyChapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VocabularyChapterMapper {

    @Mapping(target = "studentId", source = "student.studentId")
    VocabularyChapterResponse toResponse(VocabularyChapter chapter);
}

