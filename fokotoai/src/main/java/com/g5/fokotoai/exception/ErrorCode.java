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
    USERNAME_EXISTED(1000 , "Username is already existed") ,
    USERNAME_TOO_SHORT(1001,"Username must be at least 3 characters long"),
    PASSWORD_TOO_SHORT(1002,"Password must be at least 6 characters long"),
    EMAIL_INVALID(1003,"Email is invalid"),
    DOB_INVALID(1004,"Date of birth is invalid") ,
    USER_NOT_FOUND(1005,"User not found") ,
    UNCASE_EXCEPTION(1006 , "This exception is not define") ,
    WRONG_KEY_EXCEPTION(1007 , "You handler key is wrong")  ,
    USER_ALREADY_EXIST(1008 , "User already exist"),
    UNAUTHENTICATED(1009, "Unauthenticated"),
    STUDENT_NOT_FOUND(1010, "Student not found"),
    CHAPTER_NOT_FOUND(1011, "Vocabulary chapter not found"),
    CHAPTER_ACCESS_DENIED(1012, "You do not have permission to access this chapter"),
    VOCABULARY_NOT_FOUND(1013, "Vocabulary not found"),
    VOCAB_NOT_IN_CHAPTER(1014, "Vocabulary does not belong to this chapter"),
    ;
    int code ;
    String message ;
}
