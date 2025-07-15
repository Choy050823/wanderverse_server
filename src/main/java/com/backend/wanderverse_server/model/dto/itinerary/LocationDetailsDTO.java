package com.backend.wanderverse_server.model.dto.itinerary;

import com.google.maps.PlaceDetailsRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDetailsDTO {
    private String placeId;
    private String name;
    private String editorialSummary;
    private String formattedAddress;
    private List<String> openingHours;
    private double rating;
    private String website;
    private String phoneNumber;
    private String locationUrl;
    private String locationImageUrl;
}
