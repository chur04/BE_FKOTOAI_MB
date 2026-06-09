package com.g5.fokotoai.mapper;

import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.UserWordMetric;
import com.g5.fokotoai.entity.Vocabulary;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface UserWordMetricMapper {

    @Mapping(target = "mastered", constant = "false")
    @Mapping(target = "metricId", ignore = true)
    @Mapping(target = "totalWrongCount", ignore = true)
    @Mapping(target = "lastReviewedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "flashcardCorrect", ignore = true)
    @Mapping(target = "flashcardIncorrect", ignore = true)
    @Mapping(target = "quizCorrect", ignore = true)
    @Mapping(target = "quizIncorrect", ignore = true)
    UserWordMetric toDefaultMetric(Student student, Vocabulary vocab);
}