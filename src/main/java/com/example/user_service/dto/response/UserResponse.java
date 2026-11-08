package com.example.user_service.dto.response;

import com.example.user_service.util.DoctorRank;
import com.example.user_service.util.Gender;
import com.example.user_service.util.Role;
import com.example.user_service.util.UserStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String uuid;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private String fullName;
    private String phone;

    // Thông tin mở rộng của Patient
    private String address;
    private Gender gender;

    // Thông tin mở rộng của Doctor
    private DoctorRank rank;
    private String ava;
}
