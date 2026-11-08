package com.example.user_service.service;

import com.example.user_service.dto.request.AdminUserUpdateRequest;
import com.example.user_service.dto.request.UserCreateRequest;
import com.example.user_service.dto.request.UserUpdateRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.exception.AppException;
import com.example.user_service.exception.ErrorCode;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.model.Admin;
import com.example.user_service.model.Doctor;
import com.example.user_service.model.Patient;
import com.example.user_service.model.User;
import com.example.user_service.repository.AdminRepository;
import com.example.user_service.repository.DoctorRepository;
import com.example.user_service.repository.PatientRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.util.Role;
import com.example.user_service.util.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getMyInfo() {
        String uuid = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateMyInfo(UserUpdateRequest request) {
        String uuid = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                    && userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
                throw new AppException(ErrorCode.EMAIl_EXISTED);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        // Bệnh nhân được phép cập nhật địa chỉ và giới tính
        if (user instanceof Patient patient) {
            if (request.getAddress() != null && !request.getAddress().isBlank()) {
                patient.setAddress(request.getAddress());
            }
            if (request.getGender() != null) {
                patient.setGender(request.getGender());
            }
            user = patientRepository.save(patient);
        } else if (user instanceof Doctor doctor) {
            // Bác sĩ được phép cập nhật ảnh đại diện (ava)
            if (request.getAva() != null && !request.getAva().isBlank()) {
                doctor.setAva(request.getAva());
            }
            user = doctorRepository.save(doctor);
        } else {
            user = userRepository.save(user);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIl_EXISTED);
        }

        Role role = request.getRole() != null ? request.getRole() : Role.PATIENT;

        User savedUser;
        switch (role) {
            case ADMIN -> {
                Admin admin = Admin.builder()
                        .uuid(UUID.randomUUID().toString())
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .fullName(request.getFullName())
                        .phone(request.getPhone())
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build();
                savedUser = adminRepository.save(admin);
            }
            case DOCTOR -> {
                Doctor doctor = Doctor.builder()
                        .uuid(UUID.randomUUID().toString())
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .fullName(request.getFullName())
                        .phone(request.getPhone())
                        .role(Role.DOCTOR)
                        .status(UserStatus.ACTIVE)
                        .rank(request.getRank())
                        .ava(request.getAva())
                        .build();
                savedUser = doctorRepository.save(doctor);
            }
            default -> {
                Patient patient = Patient.builder()
                        .uuid(UUID.randomUUID().toString())
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .fullName(request.getFullName())
                        .phone(request.getPhone())
                        .role(role == Role.USER ? Role.USER : Role.PATIENT)
                        .status(UserStatus.INACTIVE)
                        .address(request.getAddress())
                        .gender(request.getGender())
                        .build();
                savedUser = patientRepository.save(patient);
            }
        }

        return userMapper.toUserResponse(savedUser);
    }

    public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                    && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new AppException(ErrorCode.EMAIl_EXISTED);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        // Cập nhật thông tin riêng của Patient nếu user là Patient
        if (user instanceof Patient patient) {
            if (request.getAddress() != null && !request.getAddress().isBlank()) {
                patient.setAddress(request.getAddress());
            }
            if (request.getGender() != null) {
                patient.setGender(request.getGender());
            }
            user = patientRepository.save(patient);
        } else if (user instanceof Doctor doctor) {
            // Cập nhật thông tin riêng của Doctor nếu user là Doctor
            if (request.getRank() != null) {
                doctor.setRank(request.getRank());
            }
            if (request.getAva() != null && !request.getAva().isBlank()) {
                doctor.setAva(request.getAva());
            }
            user = doctorRepository.save(doctor);
        } else {
            user = userRepository.save(user);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_FIX_ADMIN);
        }

        userRepository.delete(user);
    }

    public Page<UserResponse> getUser(Role role, int page, int size) {
        return getUser(role, null, page, size, Sort.Direction.ASC);
    }

    public Page<UserResponse> getUser(Role role, String keyword, int page, int size) {
        return getUser(role, keyword, page, size, Sort.Direction.ASC);
    }

    public Page<UserResponse> getUser(Role role, String keyword, int page, int size, Sort.Direction direction) {
        Sort.Direction dir = (direction != null) ? direction : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, "id"));
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<User> users;
        if (cleanKeyword != null && role != null) {
            users = userRepository.findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                    role, cleanKeyword, role, cleanKeyword, pageable);
        } else if (cleanKeyword != null) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    cleanKeyword, cleanKeyword, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(userMapper::toUserResponse);
    }

    public UserResponse lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_FIX_ADMIN);
        }

        user.setStatus(UserStatus.LOCKED);
        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public UserResponse unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getActiveDoctors() {
        return doctorRepository.findByStatusOrderByIdAsc(UserStatus.ACTIVE).stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public List<UserResponse> getDoctorsBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return doctorRepository.findAllById(ids).stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public List<UserResponse> getUsersBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
}