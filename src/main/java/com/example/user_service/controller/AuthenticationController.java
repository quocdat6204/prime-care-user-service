package com.example.user_service.controller;

import com.example.user_service.dto.request.ChangePasswordRequest;
import com.example.user_service.dto.request.ForgotPasswordRequest;
import com.example.user_service.dto.request.GenerateOtpRequest;
import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.LogoutRequest;
import com.example.user_service.dto.request.RefreshTokenRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.request.VerifyOtpRequest;
import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.dto.response.LoginResponse;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.service.AuthenticationService;
import com.example.user_service.service.JwtService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/authenticate")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class AuthenticationController {

    AuthenticationService authenticationService;
    JwtService jwtService;

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("User registered successfully. Please verify OTP to activate your account.")
                .result(authenticationService.register(request))
                .build();
    }

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        return ApiResponse.<LoginResponse>builder()
                .result(authenticationService.login(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                             @RequestBody(required = false) LogoutRequest request) throws ParseException {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.replace("Bearer ", "")
                : null;

        authenticationService.logout(token, request);

        return ApiResponse.<Void>builder()
                .message("Logout successfully")
                .build();
    }

    @PostMapping("/checkToken")
    void checkToken(@RequestHeader("Authorization") String authHeader) throws ParseException, JOSEException {
        String token = authHeader.replace("Bearer ", "");
        jwtService.verifyToken(token);
    }

    @PostMapping("/refresh")
    ApiResponse<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.<LoginResponse>builder()
                .message("Token refreshed successfully")
                .result(authenticationService.refreshToken(request))
                .build();
    }

    @PostMapping("/generate-otp")
    ApiResponse<String> generateOtp(@RequestBody @Valid GenerateOtpRequest request) {
        return ApiResponse.<String>builder()
                .message("OTP generated successfully")
                .result(authenticationService.generateOtp(request))
                .build();
    }

    @PostMapping("/verify-otp")
    ApiResponse<UserResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("User activated successfully")
                .result(authenticationService.verifyOtp(request))
                .build();
    }

    @PostMapping("/change-password")
    ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message("Password changed successfully")
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        return ApiResponse.<String>builder()
                .message("Password reset successfully")
                .result(authenticationService.forgotPassword(request))
                .build();
    }
}
