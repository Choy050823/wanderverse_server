package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.itinerary.TripPlanDTO;

public interface ItineraryGenerationService {
    // Generate itinerary based on user request in a prompt, and transfer into a DTO to return back
    TripPlanDTO generateItinerary(String userTripRequest);

}
