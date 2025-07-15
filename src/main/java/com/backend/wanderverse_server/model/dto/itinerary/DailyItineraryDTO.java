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
public class DailyItineraryDTO {
    private LocalDateTime date;
    private String daySummary;
    private String warnings;
    private List<TripActivityDTO> activityList;
}
