package com.example.user_service.mapper;

import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.model.Doctor;
import com.example.user_service.model.Patient;
import com.example.user_service.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .fullName(user.getFullName())
                .phone(user.getPhone());

        if (user instanceof Patient patient) {
            builder.address(patient.getAddress())
                   .gender(patient.getGender());
        } else if (user instanceof Doctor doctor) {
            builder.rank(doctor.getRank())
                   .ava(doctor.getAva());
        }

        return builder.build();
    }
}
