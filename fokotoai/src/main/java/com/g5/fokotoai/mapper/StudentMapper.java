package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.request.StudentCreateRequest;
import com.g5.fokotoai.dto.request.UpdateProfileRequest;
import com.g5.fokotoai.dto.response.StudentProfileResponse;
import com.g5.fokotoai.dto.response.StudentResponse;
import com.g5.fokotoai.entity.Student;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    Student fromStudentCreateRequestToStudent(StudentCreateRequest studentCreateRequest) ;

    StudentResponse fromStudentToStudentResponse(Student student);

    StudentProfileResponse toProfileResponse(Student student);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStudentFromRequest(UpdateProfileRequest request, @MappingTarget Student student);
}

