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
    UserWordMetric toDefaultMetric(Student student, Vocabulary vocab);
}
