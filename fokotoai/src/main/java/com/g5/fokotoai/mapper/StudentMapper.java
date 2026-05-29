package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.request.StudentCreateRequest;
import com.g5.fokotoai.dto.response.StudentResponse;
import com.g5.fokotoai.entity.Student;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    Student fromStudentCreateRequestToStudent(StudentCreateRequest studentCreateRequest) ;

    StudentResponse fromStudentToStudentResponse(Student student) ;

}
