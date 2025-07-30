package com.backend.wanderverse_server.config.agent;

import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ExternalAPIConfig {
    @Value("${google.maps.platform.api.key}")
    public String googleMapsApiKey;

    // Google Maps API Config
    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder().apiKey(googleMapsApiKey).build();
    }

    @Bean
    public List<PlaceDetailsRequest.FieldMask> placesDetailsFieldMasks() {
        return List.of(
                PlaceDetailsRequest.FieldMask.PLACE_ID,
                PlaceDetailsRequest.FieldMask.NAME,
                PlaceDetailsRequest.FieldMask.EDITORIAL_SUMMARY,
                PlaceDetailsRequest.FieldMask.FORMATTED_ADDRESS,
                PlaceDetailsRequest.FieldMask.CURRENT_OPENING_HOURS,
                PlaceDetailsRequest.FieldMask.RATING,
                PlaceDetailsRequest.FieldMask.WEBSITE,
                PlaceDetailsRequest.FieldMask.INTERNATIONAL_PHONE_NUMBER,
                PlaceDetailsRequest.FieldMask.URL,
                PlaceDetailsRequest.FieldMask.PHOTOS,
                PlaceDetailsRequest.FieldMask.GEOMETRY_LOCATION
        );
    }
}
