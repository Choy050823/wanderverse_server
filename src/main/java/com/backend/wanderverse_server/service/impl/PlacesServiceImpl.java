package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.CustomMultipartFile;
import com.backend.wanderverse_server.model.dto.itinerary.LLM_LocationDetailsDTO;
import com.backend.wanderverse_server.model.dto.itinerary.LocationDetailsDTO;
import com.backend.wanderverse_server.service.StorageService;
import com.google.maps.*;
import com.google.maps.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class PlacesServiceImpl {
    @Autowired
    private GeoApiContext injectedGeoApiContext;

    @Autowired
    private List<PlaceDetailsRequest.FieldMask> injectedPlacesDetailsFieldMasks;

    @Autowired
    private StorageService injectedStorageService;

    @Value("${google.maps.platform.api.key}")
    private String googleMapsApiKey;

    @Value("${spring.executor.thread.pool.size}")
    private int executorSize;

    private static GeoApiContext geoApiContext;

    private static List<PlaceDetailsRequest.FieldMask> placesDetailsFieldMasks;

    private static StorageService storageService;

    private static ExecutorService executorService;

    private static final int maxWidth = 1080;

    private static final int maxHeight = 1080;

    private static final Map<String, LocationDetailsDTO> localPlaceDetailsCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        PlacesServiceImpl.geoApiContext = injectedGeoApiContext;
        PlacesServiceImpl.placesDetailsFieldMasks = injectedPlacesDetailsFieldMasks;
        PlacesServiceImpl.storageService = injectedStorageService;
        PlacesServiceImpl.executorService = Executors.newFixedThreadPool(executorSize);
    }

    public static LLM_LocationDetailsDTO textSearch(String query, String placeType) {
        log.info("Performing text search for query: {} with type: {}", query, placeType);
        if (query == null || query.isEmpty()) {
            log.warn("Query for textSearch is empty");
//            return CompletableFuture.completedFuture(null);
            return null;
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                TextSearchRequest request = PlacesApi
                        .textSearchQuery(geoApiContext, query)
                        .language("en");

//                if (placeType != null && !placeType.isBlank()) {
//                    try {
//                        request.type(PlaceType.valueOf(placeType.toUpperCase()));
//                    } catch (IllegalArgumentException e) {
//                        log.warn("Invalid place Type {}, going with default place type in text search", placeType);
//                        request.type(PlaceType.TOURIST_ATTRACTION);
//                    }
//                }

                PlacesSearchResponse response = request.await();

                if (response == null || response.results.length == 0) {
                    log.warn("Empty response from text search");
                    return null;
                }

                LocationDetailsDTO locationDetails = getPlaceDetailsAsync(response.results[0].placeId).join();

                return LLM_LocationDetailsDTO.builder()
                        .placeId(locationDetails.getPlaceId())
//                        .name(locationDetails.getName())
//                        .editorialSummary(locationDetails.getEditorialSummary())
//                        .formattedAddress(locationDetails.getFormattedAddress())
//                        .rating(locationDetails.getRating())
                        .build();

            } catch (Exception e) {
                log.error("Unexpected error occurred during text search: {}", e.getMessage());
                e.printStackTrace();
                return null;
            }
        }, executorService).join();
    }

    public static List<LLM_LocationDetailsDTO> nearbySearch(double latitude, double longitude, int radius, String placeType) {
        log.info("Performing nearby search for lat: {}, long: {} with radius: {} and type: {}", latitude, longitude, radius, placeType);
        return CompletableFuture.supplyAsync(() -> {
            try {
                NearbySearchRequest request = PlacesApi
                        .nearbySearchQuery(geoApiContext, new LatLng(latitude, longitude))
                        .radius(radius)
                        .language("en");

                if (placeType != null && !placeType.isBlank()) {
                    try {
                        request.type(PlaceType.valueOf(placeType.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid place Type {}, going with default place type in nearby search", placeType);
                        request.type(PlaceType.TOURIST_ATTRACTION);
                    }
                }

                List<CompletableFuture<LocationDetailsDTO>> placeDetailsFutures =
                        Arrays.stream(request.await().results)
                                .limit(3)
                                .map(place -> getPlaceDetailsAsync(place.placeId))
                                .toList();

                CompletableFuture.allOf(placeDetailsFutures.toArray(new CompletableFuture[0])).join();

                log.info("Nearby search done!");

                return placeDetailsFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .map(locationDetails -> LLM_LocationDetailsDTO.builder()
                                .placeId(locationDetails.getPlaceId())
//                                .name(locationDetails.getName())
//                                .editorialSummary(locationDetails.getEditorialSummary())
//                                .formattedAddress(locationDetails.getFormattedAddress())
//                                .rating(locationDetails.getRating())
                                .build())
                        .toList();
            } catch (Exception e) {
                log.error("Unexpected error occurred during nearby search: {}", e.getMessage());
                e.printStackTrace();
                return null;
            }
        }, executorService).join();
    }

    public static LocationDetailsDTO getFullLocationDetailsForUser(String placeId) {
        if (placeId == null || placeId.isEmpty()) {
            log.error("Invalid place ID");
            return null;
        } else {
            if (!localPlaceDetailsCache.containsKey(placeId)) {
                try {
                    localPlaceDetailsCache.put(placeId, getPlaceDetailsAsync(placeId).join());
                } catch (Exception e) {
                    log.error("Cannot get location from placeId: {}", placeId);
                    return null;
                }
            }
            return localPlaceDetailsCache.get(placeId);
        }
    }

    private static CompletableFuture<LocationDetailsDTO> getPlaceDetailsAsync(String placeId) {
        if (localPlaceDetailsCache.containsKey(placeId)) {
            log.info("Returning place details from cache for placeId: {}", placeId);
            return CompletableFuture.completedFuture(localPlaceDetailsCache.get(placeId));
        }
        log.info("Starting to get place details for place Id: {}", placeId);
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlaceDetailsRequest request = PlacesApi
                        .placeDetails(geoApiContext, placeId)
                        .language("en");

                request.fields(placesDetailsFieldMasks.toArray(new PlaceDetailsRequest.FieldMask[0]));

                PlaceDetails placeDetails = request.await();

                if (placeDetails == null) {
                    log.warn("No place details found for placeId: {}", placeId);
                    return null;
                }

                String imageUrl = null;
                if (placeDetails.photos != null && placeDetails.photos.length > 0) {
                    imageUrl = getPlacePhotoUrlAsync(
                            placeDetails.photos[0].photoReference,
                            placeDetails.name + "-" + placeDetails.placeId
                    ).join();
                }

                LocationDetailsDTO fullLocationDetails = LocationDetailsDTO.builder()
                        .placeId(placeDetails.placeId)
                        .name(placeDetails.name)
                        .editorialSummary(placeDetails.editorialSummary != null ? placeDetails.editorialSummary.overview : "")
                        .formattedAddress(placeDetails.formattedAddress)
                        .rating(placeDetails.rating)
                        .website(placeDetails.website != null ? placeDetails.website.toString() : "")
                        .phoneNumber(placeDetails.internationalPhoneNumber != null ? placeDetails.internationalPhoneNumber : "")
                        .locationUrl(placeDetails.url != null ? placeDetails.url.toString() : "")
                        .locationImageUrl(imageUrl)
                        .openingHours(placeDetails.currentOpeningHours != null ? Arrays.asList(placeDetails.currentOpeningHours.weekdayText) : List.of())
                        .build();

                localPlaceDetailsCache.put(placeDetails.placeId, fullLocationDetails);
                log.info("Uploaded place details with place Id: {} to cache: {}",
                        placeDetails.placeId,
                        localPlaceDetailsCache.get(placeDetails.placeId).getName() + " " + localPlaceDetailsCache.get(placeDetails.placeId).getWebsite());

                return fullLocationDetails;
            } catch (Exception e) {
                log.error("Error getting Place Details with placeId {} : {}", placeId, e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error getting Place Details: " + e.getMessage());
            }
        }, executorService);
    }

    private static CompletableFuture<String> getPlacePhotoUrlAsync(String photoReference, String fileName) {

        final String savedFilename = fileName + ".jpg";
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (photoReference == null || photoReference.isEmpty()) {
                    log.error("Invalid photo reference! (Null or Empty)");
                    return null;
                }

                PhotoRequest request = PlacesApi
                        .photo(geoApiContext, photoReference)
                        .maxHeight(maxHeight)
                        .maxWidth(maxWidth);

                byte[] imageData = request.await().imageData;
                if (imageData != null && imageData.length > 0) {
                    String savedFileImageUrl =  storageService.saveFile(new CustomMultipartFile(
                            imageData, savedFilename, "image/jpeg"));
                    log.info("Successfully fetched place photo data with url: {}", savedFileImageUrl);
                    return savedFileImageUrl;
                } else {
                    log.error("Did not get any place photo!");
                    return null;
                }
            } catch (Exception e) {
                log.error("Error getting photo: {}", e.getMessage());
                return null;
            }
        }, executorService);
    }
}