package com.example.user_service.service;

import com.example.user_service.dto.JwtInfo;
import com.example.user_service.dto.TokenPayload;
import com.example.user_service.dto.request.ChangePasswordRequest;
import com.example.user_service.dto.request.ForgotPasswordRequest;
import com.example.user_service.dto.request.GenerateOtpRequest;
import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.LogoutRequest;
import com.example.user_service.dto.request.RefreshTokenRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.request.VerifyOtpRequest;
import com.example.user_service.dto.response.LoginResponse;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.exception.AppException;
import com.example.user_service.exception.ErrorCode;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.model.Patient;
import com.example.user_service.model.RedisToken;
import com.example.user_service.model.User;
import com.example.user_service.dto.event.EmailEvent;
import com.example.user_service.producer.EmailProducer;
import com.example.user_service.repository.PatientRepository;
import com.example.user_service.repository.RedisTokenRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.util.Role;
import com.example.user_service.util.UserStatus;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    AuthenticationManager authenticationManager;
    JwtService jwtService;
    RedisTokenRepository redisTokenRepository;
    UserRepository userRepository;
    PatientRepository patientRepository;
    UserMapper userMapper;
    StringRedisTemplate stringRedisTemplate;
    PasswordEncoder passwordEncoder;
    EmailProducer emailProducer;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIl_EXISTED);
        }

        Patient patient = Patient.builder()
                .uuid(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .gender(request.getGender())
                .role(Role.PATIENT)
                .status(UserStatus.INACTIVE)
                .build();

        Patient savedPatient = patientRepository.save(patient);

        return userMapper.toUserResponse(savedPatient);
    }

    public LoginResponse login(LoginRequest request){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        User user = (User) authenticate.getPrincipal();

        TokenPayload accessPayload = jwtService.generateAccessToken(user);
        TokenPayload refreshPayload = jwtService.generateRefreshToken(user);

        long refreshDiffInMillies = refreshPayload.getExpiredTime().getTime() - new Date().getTime();
        long refreshDiffInMinutes = (long) Math.ceil(refreshDiffInMillies / 60000.0);

        redisTokenRepository.save(RedisToken.builder()
                .jwtId(refreshPayload.getJwtId())
                .expiredTime(refreshDiffInMinutes)
                .build());

        // Tra ve Token
        return LoginResponse.builder()
                .accessToken(accessPayload.getToken())
                .refreshToken(refreshPayload.getToken())
                .role(user.getRole())
                .build();
    }

    public void logout(String token) throws ParseException {
        logout(token, null);
    }

    public void logout(String token, LogoutRequest request) throws ParseException {
        // 1. Thêm Access Token vào danh sách đen (Blacklist) trong Redis (giữ nguyên luồng)
        if (token != null && !token.isBlank()) {
            JwtInfo jwtInfo = jwtService.parseToken(token);
            String jwtId = jwtInfo.getJwtId();
            Date expiredTime = jwtInfo.getExpiredTime();

            if (expiredTime.after(new Date())) {
                long diffInMillies = expiredTime.getTime() - new Date().getTime();
                long diffInMinutes = (long) Math.ceil(diffInMillies / 60000.0);

                RedisToken redisToken = RedisToken.builder()
                        .jwtId(jwtId)
                        .expiredTime(diffInMinutes)
                        .build();

                redisTokenRepository.save(redisToken);
            }
        }

        // 2. Xóa Refresh Token khỏi Redis (thêm mới)
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            JwtInfo refreshJwtInfo = jwtService.parseToken(request.getRefreshToken());
            String refreshJwtId = refreshJwtInfo.getJwtId();
            redisTokenRepository.deleteById(refreshJwtId);
        }
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        try {
            String uuid = jwtService.verifyRefreshToken(request.getRefreshToken());

            User user = userRepository.findByUuid(uuid)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            if (user.getStatus() == UserStatus.LOCKED) {
                throw new AppException(ErrorCode.USER_LOCKED);
            }

            TokenPayload accessPayload = jwtService.generateAccessToken(user);

            return LoginResponse.builder()
                    .accessToken(accessPayload.getToken())
                    .role(user.getRole())
                    .build();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    public String generateOtp(GenerateOtpRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        SecureRandom random = new SecureRandom();
        String otp = String.format("%04d", random.nextInt(10000));

        String key = "OTP:" + user.getUsername();
        stringRedisTemplate.opsForValue().set(key, otp, 60, TimeUnit.SECONDS);

        EmailEvent emailEvent = EmailEvent.builder()
                .to(user.getEmail())
                .subject("Mã xác thực OTP của bạn")
                .htmlContent(String.format("""
                        <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <h2>Xin chào %s,</h2>
                            <p>Mã OTP xác thực của bạn là:</p>
                            <h1 style="color: #4CAF50; letter-spacing: 4px;">%s</h1>
                            <p>Mã này có hiệu lực trong vòng 60 giây. Vui lòng không chia sẻ cho bất kỳ ai.</p>
                        </div>
                        """, user.getFullName() != null ? user.getFullName() : user.getUsername(), otp))
                .build();
        emailProducer.sendEmailEvent(emailEvent);

        return otp;
    }

    public UserResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        String key = "OTP:" + request.getUsername();
        String storedOtp = stringRedisTemplate.opsForValue().get(key);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        stringRedisTemplate.delete(key);

        return userMapper.toUserResponse(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        String uuid = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_NOT_MATCH);
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        String newPassword = generateRandomPassword(6);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        EmailEvent emailEvent = EmailEvent.builder()
                .to(user.getEmail())
                .subject("Cấp lại mật khẩu mới")
                .htmlContent(String.format("""
                        <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <h2>Xin chào %s,</h2>
                            <p>Bạn vừa yêu cầu cấp lại mật khẩu cho tài khoản <strong>%s</strong>.</p>
                            <p>Mật khẩu mới của bạn là:</p>
                            <h2 style="background: #f4f4f4; padding: 10px; display: inline-block; border-radius: 4px; color: #d9534f;">%s</h2>
                            <p>Vui lòng đăng nhập và đổi lại mật khẩu ngay để bảo đảm an toàn.</p>
                        </div>
                        """, user.getFullName() != null ? user.getFullName() : user.getUsername(), user.getUsername(), newPassword))
                .build();
        emailProducer.sendEmailEvent(emailEvent);

        return newPassword;
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

