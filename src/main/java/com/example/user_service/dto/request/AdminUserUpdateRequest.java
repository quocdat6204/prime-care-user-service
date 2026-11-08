package com.example.user_service.dto.request;

import com.example.user_service.util.DoctorRank;
import com.example.user_service.util.Gender;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest {

    @Email(message = "Email format is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    @JsonProperty("full_name")
    @JsonAlias("full_name")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    // Dành cho Patient
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private Gender gender;

    // Dành cho Doctor
    private DoctorRank rank;

    private String ava;
}
