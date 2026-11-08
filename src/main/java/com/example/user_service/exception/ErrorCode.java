package com.example.user_service.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_EXISTED(409, "Username already exists", HttpStatus.CONFLICT),
    EMAIl_EXISTED(409, "Email already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    CANNOT_FIX_ADMIN(403, "Admin cannot be fixed", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(401, "Username or password is wrong", HttpStatus.UNAUTHORIZED),


    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(403, "Do not have permission", HttpStatus.FORBIDDEN),
    USER_INACTIVE(401, "User account is inactive", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(400, "Invalid or expired OTP", HttpStatus.BAD_REQUEST),
    USER_LOCKED(401, "User account is locked", HttpStatus.UNAUTHORIZED),
    INVALID_OLD_PASSWORD(400, "Old password is incorrect", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_MATCH(400, "Email does not match with username", HttpStatus.BAD_REQUEST)
    ;

    private int code;

    private String message;

    private HttpStatusCode statusCode;

}
