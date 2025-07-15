package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.itinerary.TripPlanDTO;
import com.backend.wanderverse_server.service.ItineraryGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/itinerary")
public class ItineraryController {
    @Autowired
    private ItineraryGenerationService itineraryGenerationService;

    @GetMapping
    public ResponseEntity<TripPlanDTO> getItinerary(@RequestParam String userTripRequest) {
        TripPlanDTO tripPlanDTO = itineraryGenerationService.generateItinerary(userTripRequest);
        return tripPlanDTO != null
                ? ResponseEntity.ok().body(tripPlanDTO)
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
