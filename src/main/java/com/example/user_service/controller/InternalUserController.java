package com.example.user_service.controller;

import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller phục vụ các API nội bộ giao tiếp giữa các microservice (Clinic, v.v.).
 */
@RestController
@RequestMapping("/users/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserController {

    UserService userService;

    @PostMapping("/doctors/batch")
    public ApiResponse<List<UserResponse>> getDoctorsBatch(@RequestBody List<Long> ids) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getDoctorsBatch(ids))
                .build();
    }

    @PostMapping("/users/batch")
    public ApiResponse<List<UserResponse>> getUsersBatch(@RequestBody List<Long> ids) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsersBatch(ids))
                .build();
    }
}
