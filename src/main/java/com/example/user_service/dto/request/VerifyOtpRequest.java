package com.example.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyOtpRequest {

    @NotBlank(message = "Username cannot be blank")
    String username;

    @NotBlank(message = "OTP cannot be blank")
    String otp;
}
