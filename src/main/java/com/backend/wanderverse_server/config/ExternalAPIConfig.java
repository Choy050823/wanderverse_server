package com.backend.wanderverse_server.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalAPIConfig {
    @Value("${google.maps.platform.api.key}")
    public String googleMapsApiKey;

    // Google Maps API Config
    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder().apiKey(googleMapsApiKey).build();
    }
}
