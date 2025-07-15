package com.backend.wanderverse_server.model.dto.itinerary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TripOverviewDTO {
    private String planTitle;
    private int tripDurationDays;
    private LocalDateTime tripStartDate;
    private String mainDestination;
    List<String> keyInterests;
    List<DailyThemesDTO> dailyThemes;
}
