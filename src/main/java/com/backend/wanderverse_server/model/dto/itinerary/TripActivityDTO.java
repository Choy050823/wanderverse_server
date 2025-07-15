package com.backend.wanderverse_server.model.dto.itinerary;

import com.backend.wanderverse_server.model.dto.post.DestinationDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripActivityDTO {
    private ActivityType activityType;
    private LocalDateTime estimatedStartTime;
    private LocalDateTime estimatedEndTime;

    // Fields specific to 'destination' type (will be null for 'travel')
    private LocationDetailsDTO locationDetails;

    // Fields specific to 'travel' type (will be null for 'destination')
    private TravelDetailsDTO travelDetails;

    public enum ActivityType {
        destination,
        travel
    }
}
