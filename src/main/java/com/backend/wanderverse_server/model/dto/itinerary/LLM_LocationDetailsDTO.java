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
//    private String editorialSummary;
//    private String formattedAddress;
//    private double rating;

    @Override
    public String toString() {
        return "[" + "placeId: " + placeId
//                + " name: " + name
//                + " editorialSummary: " +editorialSummary
//                + " formattedAddress: " + formattedAddress
//                + " rating: " + rating
                + "]\n";
    }
}
