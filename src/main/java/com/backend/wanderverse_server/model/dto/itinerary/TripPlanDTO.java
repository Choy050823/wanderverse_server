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
public class TripPlanDTO {
    private String planTitle;
    private String overview;
    private List<String> warnings;
    private LocalDateTime tripStartDate;
    private LocalDateTime tripEndDate;
    private List<DailyItineraryDTO> dailyItineraryList;
}
