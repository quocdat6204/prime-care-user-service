package com.example.user_service.dto.response.statistic;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientOverviewResponse {
    long totalRegistered;
    long newThisMonth;
}
