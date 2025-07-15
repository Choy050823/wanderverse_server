package com.backend.wanderverse_server.model.dto.itinerary;

import com.google.maps.model.TravelMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TravelDetailsDTO {
    private String origin;
    private String destination;
    private TravelMode mode;
    private Long durationMinutes;
    private Double distanceKm;
//    private String directionsUrl;

//    public enum TravelMode {
//        DRIVING,
//        WALKING,
//        BICYCLING,
//        TRANSIT
//    }
}
