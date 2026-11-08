package com.example.user_service.controller;

import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.dto.response.statistic.DoctorOverviewResponse;
import com.example.user_service.dto.response.statistic.PatientGrowthResponse;
import com.example.user_service.dto.response.statistic.PatientOverviewResponse;
import com.example.user_service.service.UserStatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller nội bộ phục vụ statistic-service gọi qua OpenFeign.
 */
@RestController
@RequestMapping("/users/internal/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalStatisticController {

    UserStatisticService userStatisticService;

    @GetMapping("/patient-overview")
    public ApiResponse<PatientOverviewResponse> getPatientOverview(
            @RequestParam int month,
            @RequestParam int year) {
        return ApiResponse.<PatientOverviewResponse>builder()
                .result(userStatisticService.getPatientOverview(month, year))
                .build();
    }

    @GetMapping("/doctor-overview")
    public ApiResponse<DoctorOverviewResponse> getDoctorOverview() {
        return ApiResponse.<DoctorOverviewResponse>builder()
                .result(userStatisticService.getDoctorOverview())
                .build();
    }

    @GetMapping("/patients/growth")
    public ApiResponse<PatientGrowthResponse> getPatientGrowth(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "MONTH") String groupBy) {
        return ApiResponse.<PatientGrowthResponse>builder()
                .result(userStatisticService.getPatientGrowth(from, to, groupBy))
                .build();
    }
}
