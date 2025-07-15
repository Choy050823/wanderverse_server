package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.itinerary.TravelDetailsDTO;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RoutesServiceImpl {
    @Autowired
    private GeoApiContext injectedGeoApiContext;

    private static GeoApiContext geoApiContext;

    @PostConstruct
    public void init() {
        RoutesServiceImpl.geoApiContext = injectedGeoApiContext;
    }

    public static List<TravelDetailsDTO> getBasicRoute(String origin, String destination, String travelMode) {
        log.info("Calculating basic route from {} to {} via {}", origin, destination, travelMode);

        DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                .origin(origin)
                .destination(destination)
                .mode(TravelMode.valueOf(travelMode.toUpperCase()));

        return executeRouteRequest(request, TravelMode.valueOf(travelMode.toUpperCase()));
    }

    public static List<TravelDetailsDTO> getFixedWaypointRoute(String origin, String destination, String fixedIntermediateWaypoints, String travelMode, String departureTime) {
        try {
            log.info("Calculating fixed waypoint route from {} to {} via {} with waypoints: {}", origin, destination, travelMode, fixedIntermediateWaypoints);
            String[] intermediateWayPointArr = fixedIntermediateWaypoints.split(",");

            DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .mode(TravelMode.valueOf(travelMode.toUpperCase()))
                    .optimizeWaypoints(false)
                    .waypoints(intermediateWayPointArr)
                    .departureTime(Instant.parse(departureTime))
                    .trafficModel(TrafficModel.BEST_GUESS);

            return executeRouteRequest(request, TravelMode.valueOf(travelMode.toUpperCase()));
        } catch (Exception e) {
            log.error("Error computing routes for fixed waypoint: {}", e.getMessage());
            return null;
        }
    }

    public static List<TravelDetailsDTO> getOptimizedWaypointRoute(String origin, String destination, String intermediateWaypoints, String travelMode, String departureTime) {
        try {
            log.info("Calculating optimized waypoint route from {} to {} via {} with waypoints: {}", origin, destination, travelMode, intermediateWaypoints);
            String[] intermediateWayPointArr = intermediateWaypoints.split(",");

            DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                    .origin(origin)
                    .destination(destination)
                    .mode(TravelMode.valueOf(travelMode.toUpperCase()))
                    .waypoints(intermediateWayPointArr)
                    .optimizeWaypoints(true)
                    .departureTime(Instant.parse(departureTime))
                    .trafficModel(TrafficModel.BEST_GUESS);

            return executeRouteRequest(request, TravelMode.valueOf(travelMode));
        } catch (Exception e) {
            log.error("Error computing routes for optimized waypoint: {}", e.getMessage());
            return null;
        }
    }

    public static List<TravelDetailsDTO> getRouteWithAvoidance(String origin, String destination, String travelMode, boolean avoidTolls, boolean avoidHighways, boolean avoidFerries, String departureTime) {
        DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                .origin(origin)
                .destination(destination)
                .mode(TravelMode.valueOf(travelMode.toUpperCase()))
                .optimizeWaypoints(true)
                .departureTime(Instant.parse(departureTime))
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

        return executeRouteRequest(request, TravelMode.valueOf(travelMode.toUpperCase()));
    }

    private static List<TravelDetailsDTO> executeRouteRequest(DirectionsApiRequest request, TravelMode travelMode) {
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

//            List<String> orderedWaypoint = new ArrayList<>();
//            orderedWaypoint.add(origin);
//
//            if (optimizeWayPoints && bestRoute.waypointOrder != null) {
//                for (int originalIndex : bestRoute.waypointOrder) {
//                    orderedWaypoint.add(originalIntermediateWaypoints.get(originalIndex));
//                }
//            } else if (!originalIntermediateWaypoints.isEmpty()) {
//                orderedWaypoint.addAll(originalIntermediateWaypoints);
//            }
//
//            orderedWaypoint.add(destination);
//
//            long totalDurationMinutes = Arrays
//                    .stream(bestRoute.legs)
//                    .mapToLong(leg -> leg.durationInTraffic != null
//                            ? leg.durationInTraffic.inSeconds
//                            : leg.duration.inSeconds)
//                    .sum() / 60;
//
//            long totalDistanceInKm = Arrays.stream(bestRoute.legs)
//                    .mapToLong(leg -> leg.distance.inMeters / 1000)
//                    .sum();
//
//            log.info("Route computed. Total Duration: {} minutes, total distance: {} km", totalDurationMinutes, totalDistanceInKm);
        } catch (Exception e) {
            log.error("Error computing route: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
