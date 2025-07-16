package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.itinerary.TravelDetailsDTO;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class RoutesServiceImpl {
    @Autowired
    private GeoApiContext injectedGeoApiContext;

    @Value("${spring.executor.thread.pool.size}")
    private int executorSize;

    private static GeoApiContext geoApiContext;

    private static ExecutorService executorService;

    @PostConstruct
    public void init() {
        RoutesServiceImpl.geoApiContext = injectedGeoApiContext;
        RoutesServiceImpl.executorService = Executors.newFixedThreadPool(executorSize);
    }

    public static List<TravelDetailsDTO> getBasicRoute(String origin, String destination, String travelMode) {
        log.info("Calculating basic route from {} to {} via {}", origin, destination, travelMode);
        return CompletableFuture.supplyAsync(() -> DirectionsApi.newRequest(geoApiContext)
                .origin(origin)
                .destination(destination)
                .mode(TravelMode.valueOf(travelMode.toUpperCase())), executorService)
                .thenComposeAsync(request ->
                        executeRouteRequest(request, TravelMode.valueOf(travelMode.toUpperCase()))).join();
    }

    public static List<TravelDetailsDTO> getFixedWaypointRoute(String origin, String destination, String fixedIntermediateWaypoints, String travelMode, String departureTime) {
        log.info("Calculating fixed waypoint route from {} to {} via {} with waypoints: {}",
                origin, destination, travelMode, fixedIntermediateWaypoints);

        return CompletableFuture.supplyAsync(() -> {
                    try {
                        String[] intermediateWayPointArr = fixedIntermediateWaypoints.split(",");

                        return DirectionsApi.newRequest(geoApiContext)
                                .origin(origin)
                                .destination(destination)
                                .mode(TravelMode.valueOf(travelMode.toUpperCase()))
                                .waypoints(intermediateWayPointArr)
                                .optimizeWaypoints(true)
                                .departureTime(parseDepartureTime(departureTime))
                                .trafficModel(TrafficModel.OPTIMISTIC);
                    } catch (Exception e) {
                        log.error("Error computing routes for fixed waypoint: {}", e.getMessage());
                        return null;
                    }
                }, executorService)
                .thenComposeAsync(request ->
                        executeRouteRequest(request, TravelMode.valueOf(travelMode))).join();
    }

    public static List<TravelDetailsDTO> getOptimizedWaypointRoute(String origin, String destination, String intermediateWaypoints, String travelMode, String departureTime) {
        log.info("Calculating optimized waypoint route from {} to {} via {} with waypoints: {}",
                origin, destination, travelMode, intermediateWaypoints);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String[] intermediateWayPointArr = intermediateWaypoints.split(",");

                return DirectionsApi.newRequest(geoApiContext)
                        .origin(origin)
                        .destination(destination)
                        .mode(TravelMode.valueOf(travelMode.toUpperCase()))
                        .waypoints(intermediateWayPointArr)
                        .optimizeWaypoints(true)
                        .departureTime(parseDepartureTime(departureTime))
                        .trafficModel(TrafficModel.BEST_GUESS);
            } catch (Exception e) {
                log.error("Error computing routes for optimized waypoint: {}", e.getMessage());
                return null;
            }
        }, executorService)
                .thenComposeAsync(request ->
                        executeRouteRequest(request, TravelMode.valueOf(travelMode))).join();

    }

    public static List<TravelDetailsDTO> getRouteWithAvoidance(String origin, String destination, String travelMode, boolean avoidTolls, boolean avoidHighways, boolean avoidFerries, String departureTime) {
        log.info("Calculating route with avoidance from {} to {} via {} with avoidTolls: {}, avoidHighway: {}, avoidFerries: {}",
                origin, destination, travelMode, avoidTolls, avoidHighways, avoidFerries);

        return CompletableFuture.supplyAsync(() -> {
            DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .mode(TravelMode.valueOf(travelMode.toUpperCase()))
                    .optimizeWaypoints(true)
                    .departureTime(parseDepartureTime(departureTime))
                    .trafficModel(TrafficModel.BEST_GUESS);

            if (avoidTolls) {
                request.avoid(DirectionsApi.RouteRestriction.TOLLS);
            }

            if (avoidHighways) {
                request.avoid(DirectionsApi.RouteRestriction.HIGHWAYS);
            }

            if (avoidFerries) {
                request.avoid(DirectionsApi.RouteRestriction.FERRIES);
            }

            return request;
        }, executorService)
                .thenComposeAsync(request ->
                        executeRouteRequest(request, TravelMode.valueOf(travelMode.toUpperCase()))).join();
    }

    // In src/main/java/com/backend/wanderverse_server/service/impl/RoutesServiceImpl.java

    private static Instant parseDepartureTime(String departureTime) {
        try {
            String formattedDepartureTime = departureTime;
            // Check if time part exists and if seconds are missing
            if (formattedDepartureTime.contains("T") && formattedDepartureTime.split(":").length == 2) {
                // Append seconds if they are missing
                formattedDepartureTime = formattedDepartureTime.replace("Z", "") + ":00";
            }

            // Ensure the string ends with a UTC zone identifier if it doesn't have an offset
            if (!formattedDepartureTime.endsWith("Z") && !formattedDepartureTime.matches(".*[+-]\\d{2}:\\d{2}$")) {
                formattedDepartureTime += "Z";
            }
            log.info("Formatted departureTime for parsing: {}", formattedDepartureTime);
            return Instant.parse(formattedDepartureTime);
        } catch (DateTimeParseException e) {
            log.warn("Invalid departureTime format: {}. Falling back to current time + 5 minutes.", departureTime);
            // Fallback to a safe default
            return Instant.now().plusSeconds(300).truncatedTo(ChronoUnit.SECONDS);
        }
    }

    private static CompletableFuture<List<TravelDetailsDTO>> executeRouteRequest(
            DirectionsApiRequest request, TravelMode travelMode) {
        log.info("Executing route request from Google Routes API...");
        return CompletableFuture.supplyAsync(() -> {
            try {
                DirectionsResult result = request.await();
                if (result == null || result.routes == null || result.routes.length == 0) {
                    log.warn("No route found for request: {}", request.toString());
                    return null;
                }

                DirectionsRoute bestRoute = result.routes[0];
                String origin = bestRoute.legs[0].startAddress;
                String destination = bestRoute.legs[bestRoute.legs.length - 1].endAddress;

                log.info("Route found! Origin {}", origin);
                log.info("Route found! Destination {}", destination);

                return Arrays.stream(bestRoute.legs).map(leg -> TravelDetailsDTO.builder()
                        .origin(leg.startAddress)
                        .destination(leg.endAddress)
                        .durationMinutes(leg.duration.inSeconds / 60)
                        .distanceKm((double) leg.distance.inMeters / 1000)
                        .mode(travelMode)
                        .build()
                ).toList();
            } catch (Exception e) {
                log.error("Error computing route: {}", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error computing route: " + e.getMessage());
            }
        }, executorService);

    }
}
