package com.backend.wanderverse_server.model.dto.itinerary;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class LocationDetailsDTO {
    private String placeId;
    private String name;
    private String editorialSummary;
    private String formattedAddress;
    private double latitude;
    private double longitude;
    private List<String> openingHours;
    private double rating;
    private String website;
    private String phoneNumber;
    private String locationUrl;
    private String locationImageUrl;
}
