package com.g5.fokotoai.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    INVALID_KEY(9998, "Invalid error key"),
    UNAUTHENTICATED(9997, "Unauthenticated"),
    UNAUTHORIZED(9996, "You do not have permission"),
    UNCASE_EXCEPTION(9995 , "This exception is undedefined") ,
    WRONG_KEY_EXCEPTION(9994 , "You handler wrong key in exception handler") ,

    FULLNAME_REQUIRED(1000, "Fullname is required"),
    FULLNAME_INVALID(1001, "Fullname must be between 5 and 100 characters"),

    USERNAME_REQUIRED(1002, "Username is required"),
    USERNAME_TOO_SHORT(1003, "Username must be at least 3 characters long"),
    USERNAME_EXISTED(1004, "Username already exists"),

    PASSWORD_REQUIRED(1005, "Password is required"),
    PASSWORD_TOO_SHORT(1006, "Password must be at least 6 characters long"),

    EMAIL_REQUIRED(1007, "Email is required"),
    EMAIL_INVALID(1008, "Email is invalid"),
    EMAIL_EXISTED(1009, "Email already exists"),

    LEVEL_REQUIRED(1010, "Japanese level is required"),


    STUDENT_NOT_FOUND(1100, "Student not found");

    ;
    int code ;
    String message ;
}
