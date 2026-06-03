package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.request.VocabularyChapterRequest;
import com.g5.fokotoai.dto.response.VocabularyChapterResponse;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.VocabularyChapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VocabularyChapterMapper {

    @Mapping(target = "studentId", source = "student.studentId")
    VocabularyChapterResponse toResponse(VocabularyChapter chapter);

    // student → auto-mapped (tên trùng parameter)
    // chapterName, level, orderIndex, description → auto-mapped từ request (chỉ tồn tại trong request)
    @Mapping(target = "chapterId", ignore = true)
    VocabularyChapter toEntity(VocabularyChapterRequest request, Student student);

    @Mapping(target = "chapterId", ignore = true)
    @Mapping(target = "student", ignore = true)
    void updateFromRequest(VocabularyChapterRequest request, @MappingTarget VocabularyChapter chapter);
}
