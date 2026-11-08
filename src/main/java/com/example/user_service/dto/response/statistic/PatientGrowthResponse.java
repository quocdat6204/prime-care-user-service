package com.example.user_service.dto.response.statistic;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientGrowthResponse {

    long totalPatients;
    List<GrowthItem> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthItem {
        String label;
        long newPatients;
        long cumulativeTotal;
    }
}
