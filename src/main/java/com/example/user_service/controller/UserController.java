package com.example.user_service.controller;

import com.example.user_service.dto.request.AdminUserUpdateRequest;
import com.example.user_service.dto.request.UserCreateRequest;
import com.example.user_service.dto.request.UserUpdateRequest;
import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.example.user_service.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping("/my-info")
    ApiResponse<UserResponse> updateMyInfo(@RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyInfo(request))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<UserResponse> updateUser(@PathVariable Long id, @RequestBody @Valid AdminUserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(id, request))
                .build();
    }

    /**
     * Lấy danh sách toàn bộ bác sĩ đang hoạt động (Public endpoint dành cho bệnh nhân và khách vãng lai).
     */
    @GetMapping("/doctors")
    public ApiResponse<List<UserResponse>> getDoctors() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getActiveDoctors())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.<Void>builder()
                .message("User has been deleted successfully")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Page<UserResponse>> getUser(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getUser(role, keyword, page, size, sortDirection))
                .build();
    }

    @PutMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<UserResponse> lockUser(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .message("User locked successfully")
                .result(userService.lockUser(id))
                .build();
    }

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<UserResponse> unlockUser(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .message("User unlocked successfully")
                .result(userService.unlockUser(id))
                .build();
    }
}
