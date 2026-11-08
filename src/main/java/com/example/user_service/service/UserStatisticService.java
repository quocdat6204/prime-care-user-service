package com.example.user_service.service;

import com.example.user_service.dto.response.statistic.DoctorOverviewResponse;
import com.example.user_service.dto.response.statistic.PatientGrowthResponse;
import com.example.user_service.dto.response.statistic.PatientOverviewResponse;
import com.example.user_service.model.Patient;
import com.example.user_service.repository.DoctorRepository;
import com.example.user_service.repository.PatientRepository;
import com.example.user_service.util.UserStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional(readOnly = true)
public class UserStatisticService {

    PatientRepository patientRepository;
    DoctorRepository doctorRepository;

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private Instant parseStartOfDay(String dateStr) {
        return LocalDate.parse(dateStr).atStartOfDay(DEFAULT_ZONE).toInstant();
    }

    private Instant parseEndOfDay(String dateStr) {
        return LocalDate.parse(dateStr).plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();
    }

    public PatientOverviewResponse getPatientOverview(int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        Instant start = ym.atDay(1).atStartOfDay(DEFAULT_ZONE).toInstant();
        Instant end = ym.plusMonths(1).atDay(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        long totalRegistered = patientRepository.count();
        long newThisMonth = 0;
        try {
            newThisMonth = patientRepository.countByCreatedAtBetween(start, end);
        } catch (Exception e) {
            log.warn("Error counting new patients: {}", e.getMessage());
        }

        return PatientOverviewResponse.builder()
                .totalRegistered(totalRegistered)
                .newThisMonth(newThisMonth)
                .build();
    }

    public DoctorOverviewResponse getDoctorOverview() {
        long activeCount = 0;
        try {
            activeCount = doctorRepository.countByStatus(UserStatus.ACTIVE);
        } catch (Exception e) {
            log.warn("Error counting active doctors: {}", e.getMessage());
            activeCount = doctorRepository.count();
        }

        return DoctorOverviewResponse.builder()
                .totalActive(activeCount)
                .build();
    }

    public PatientGrowthResponse getPatientGrowth(String from, String to, String groupBy) {
        Instant start = parseStartOfDay(from);
        Instant end = parseEndOfDay(to);

        long totalAll = patientRepository.count();
        List<Patient> list = new ArrayList<>();
        try {
            list = patientRepository.findByCreatedAtBetween(start, end);
        } catch (Exception e) {
            log.warn("Error fetching patients by createdAt: {}", e.getMessage());
        }

        DateTimeFormatter formatter = "YEAR".equalsIgnoreCase(groupBy)
                ? DateTimeFormatter.ofPattern("yyyy").withZone(DEFAULT_ZONE)
                : ("DAY".equalsIgnoreCase(groupBy)
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(DEFAULT_ZONE)
                : DateTimeFormatter.ofPattern("yyyy-MM").withZone(DEFAULT_ZONE));

        // Bỏ qua các patient có createdAt == null
        Map<String, List<Patient>> grouped = list.stream()
                .filter(p -> p.getCreatedAt() != null)
                .collect(Collectors.groupingBy(p -> formatter.format(p.getCreatedAt())));

        List<PatientGrowthResponse.GrowthItem> items = new ArrayList<>();
        long cumulative = 0;
        for (Map.Entry<String, List<Patient>> entry : grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList())) {
            long newCount = entry.getValue().size();
            cumulative += newCount;
            items.add(PatientGrowthResponse.GrowthItem.builder()
                    .label(entry.getKey())
                    .newPatients(newCount)
                    .cumulativeTotal(cumulative)
                    .build());
        }

        return PatientGrowthResponse.builder()
                .totalPatients(totalAll)
                .data(items)
                .build();
    }
}
