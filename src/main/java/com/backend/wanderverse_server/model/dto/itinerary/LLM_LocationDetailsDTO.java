package com.backend.wanderverse_server.model.dto.itinerary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LLM_LocationDetailsDTO {
    private String placeId;
    private String name;
    private double latitude;
    private double longitude;
}
