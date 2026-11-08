package com.example.user_service.configuration;

import com.example.user_service.model.Admin;
import com.example.user_service.model.Doctor;
import com.example.user_service.model.Patient;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.util.DoctorRank;
import com.example.user_service.util.Gender;
import com.example.user_service.util.Role;
import com.example.user_service.util.UserStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DataInitializer implements CommandLineRunner {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Tạo tài khoản ADMIN mặc định nếu chưa tồn tại
        if (!userRepository.existsByUsername("admin")) {
            Admin admin = Admin.builder()
                    .uuid(UUID.randomUUID().toString())
                    .username("admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .phone("0987654321")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
            log.info("Initialized default ADMIN account: username='admin', password='Admin@123'");
        }

        // Tạo tài khoản PATIENT mặc định nếu chưa tồn tại
        if (!userRepository.existsByUsername("user")) {
            Patient user = Patient.builder()
                    .uuid(UUID.randomUUID().toString())
                    .username("user")
                    .email("user@gmail.com")
                    .password(passwordEncoder.encode("User@123"))
                    .fullName("Regular User")
                    .phone("0123456789")
                    .role(Role.PATIENT)
                    .status(UserStatus.ACTIVE)
                    .address("Số 123 Đường Nguyễn Trãi, Thanh Xuân, Hà Nội")
                    .gender(Gender.MALE)
                    .build();

            userRepository.save(user);
            log.info("Initialized default USER account: username='user', password='User@123'");
        }

        // Bác sĩ 1: Giáo sư
        if (!userRepository.existsByUsername("doctor_a")) {
            Doctor doctor1 = Doctor.builder()
                    .uuid(UUID.randomUUID().toString())
                    .username("doctor_a")
                    .email("doctor_a@hospital.com")
                    .password(passwordEncoder.encode("Doctor@123"))
                    .fullName("GS. TS. Nguyễn Văn A")
                    .phone("0912345671")
                    .role(Role.DOCTOR)
                    .status(UserStatus.ACTIVE)
                    .rank(DoctorRank.PROFESSOR)
                    .ava("https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400")
                    .build();

            userRepository.save(doctor1);
            log.info("Initialized default DOCTOR account: username='doctor_a' (GS. TS. Nguyễn Văn A)");
        }

        // Bác sĩ 2: Tiến sĩ
        if (!userRepository.existsByUsername("doctor_b")) {
            Doctor doctor2 = Doctor.builder()
                    .uuid(UUID.randomUUID().toString())
                    .username("doctor_b")
                    .email("doctor_b@hospital.com")
                    .password(passwordEncoder.encode("Doctor@123"))
                    .fullName("TS. BS. Trần Thị B")
                    .phone("0912345672")
                    .role(Role.DOCTOR)
                    .status(UserStatus.ACTIVE)
                    .rank(DoctorRank.DOCTORATE)
                    .ava("https://images.unsplash.com/photo-1594824813629-873b222956cf?w=400")
                    .build();

            userRepository.save(doctor2);
            log.info("Initialized default DOCTOR account: username='doctor_b' (TS. BS. Trần Thị B)");
        }

        // Bác sĩ 3: Thạc sĩ
        if (!userRepository.existsByUsername("doctor_c")) {
            Doctor doctor3 = Doctor.builder()
                    .uuid(UUID.randomUUID().toString())
                    .username("doctor_c")
                    .email("doctor_c@hospital.com")
                    .password(passwordEncoder.encode("Doctor@123"))
                    .fullName("ThS. BS. Lê Quang C")
                    .phone("0912345673")
                    .role(Role.DOCTOR)
                    .status(UserStatus.ACTIVE)
                    .rank(DoctorRank.MASTER)
                    .ava("https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=400")
                    .build();

            userRepository.save(doctor3);
            log.info("Initialized default DOCTOR account: username='doctor_c' (ThS. BS. Lê Quang C)");
        }
    }
}
