package com.backend.wanderverse_server.service.impl;

//import com.backend.wanderverse_server.service.PlacesService;
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
import java.util.concurrent.ConcurrentHashMap;

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
    private static String googleMapsApiKey;

    private static GeoApiContext geoApiContext;

    private static List<PlaceDetailsRequest.FieldMask> placesDetailsFieldMasks;

    private static StorageService storageService;

    private static final int maxWidth = 1080;

    private static final int maxHeight = 1080;

    private static final Map<String, LocationDetailsDTO> localPlaceDetailsCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        PlacesServiceImpl.geoApiContext = injectedGeoApiContext;
        PlacesServiceImpl.placesDetailsFieldMasks = injectedPlacesDetailsFieldMasks;
        PlacesServiceImpl.storageService = injectedStorageService;
    }

    public static LLM_LocationDetailsDTO textSearch(String query, String placeType) {
        try {
            TextSearchRequest request = PlacesApi
                    .textSearchQuery(geoApiContext, query)
                    .language("en");

            if (placeType != null && !placeType.isBlank()) {
                try {
                    request.type(PlaceType.valueOf(placeType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid place Type {}, going with default place type in text search", placeType);
                    request.type(PlaceType.TOURIST_ATTRACTION);
                }
            }

            PlaceDetails placeDetails = getPlaceDetails(request.await().results[0].placeId);
            assert placeDetails != null;

            LocationDetailsDTO fullLocationDetails = LocationDetailsDTO.builder()
                    .placeId(placeDetails.placeId)
                    .name(placeDetails.name)
                    .editorialSummary(placeDetails.editorialSummary != null ? placeDetails.editorialSummary.overview : "")
                    .formattedAddress(placeDetails.formattedAddress)
                    .rating(placeDetails.rating)
                    .website(placeDetails.website != null ? placeDetails.website.toString() : "")
                    .phoneNumber(placeDetails.internationalPhoneNumber != null ? placeDetails.internationalPhoneNumber : "")
                    .locationUrl(placeDetails.url != null ? placeDetails.url.toString() : "")
                    .locationImageUrl(getPlacePhotoUrl(placeDetails.photos[0].photoReference, placeDetails.name + "-" + placeDetails.placeId))
                    .openingHours(placeDetails.currentOpeningHours != null ? Arrays.asList(placeDetails.currentOpeningHours.weekdayText) : List.of())
                    .build();

            localPlaceDetailsCache.put(placeDetails.placeId, fullLocationDetails);

            return LLM_LocationDetailsDTO.builder()
                    .placeId(placeDetails.placeId)
                    .name(placeDetails.name)
                    .editorialSummary(placeDetails.editorialSummary != null ? placeDetails.editorialSummary.overview : "")
                    .formattedAddress(placeDetails.formattedAddress)
                    .rating(placeDetails.rating)
                    .build();

        } catch (Exception e) {
            log.error("Unexpected error occurred during text search: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static List<LLM_LocationDetailsDTO> nearbySearch(double locationLat, double locationLong, int radius, String placeType) {
        try {
            NearbySearchRequest request = PlacesApi
                    .nearbySearchQuery(geoApiContext, new LatLng(locationLat, locationLong))
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

            return Arrays.stream(request.await().results)
                    .map(place -> getPlaceDetails(place.placeId)).filter(Objects::nonNull)
                    .map(placeDetails -> {
                        LocationDetailsDTO fullLocationDetails = LocationDetailsDTO.builder()
                                .placeId(placeDetails.placeId)
                                .name(placeDetails.name)
                                .editorialSummary(placeDetails.editorialSummary != null ? placeDetails.editorialSummary.overview : "")
                                .formattedAddress(placeDetails.formattedAddress)
                                .rating(placeDetails.rating)
                                .website(placeDetails.website != null ? placeDetails.website.toString() : "")
                                .phoneNumber(placeDetails.internationalPhoneNumber != null ? placeDetails.internationalPhoneNumber : "")
                                .locationUrl(placeDetails.url != null ? placeDetails.url.toString() : "")
                                .locationImageUrl(getPlacePhotoUrl(placeDetails.photos[0].photoReference, placeDetails.name + "-" + placeDetails.placeId))
                                .openingHours(placeDetails.currentOpeningHours != null ? Arrays.asList(placeDetails.currentOpeningHours.weekdayText) : List.of())
                                .build();

                        localPlaceDetailsCache.put(placeDetails.placeId, fullLocationDetails);

                        return LLM_LocationDetailsDTO.builder()
                                .placeId(placeDetails.placeId)
                                .name(placeDetails.name)
                                .editorialSummary(placeDetails.editorialSummary != null ? placeDetails.editorialSummary.overview : "")
                                .formattedAddress(placeDetails.formattedAddress)
                                .rating(placeDetails.rating)
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Unexpected error occurred during nearby search: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static LocationDetailsDTO getFullLocationDetailsForUser(String placeId) {
        if (placeId == null || placeId.isEmpty()) {
            log.error("Invalid place ID");
            return null;
        } else {
            return localPlaceDetailsCache.get(placeId);
        }
    }

    private static PlaceDetails getPlaceDetails(String placeId) {
        try {
            PlaceDetailsRequest request = PlacesApi
                    .placeDetails(geoApiContext, placeId)
                    .language("en");

            request.fields(placesDetailsFieldMasks.toArray(new PlaceDetailsRequest.FieldMask[0]));

            return request.await();
        } catch (Exception e) {
            log.error("Error getting Place Details: {}", e.getMessage());
            return null;
        }
    }

    private static String getPlacePhotoUrl(String photoReference, String fileName) {
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
                log.info("Successfully fetched place photo data");
                fileName = fileName + ".jpg";
                return storageService.saveFile(new CustomMultipartFile(imageData, fileName, "image/jpeg"));
            } else {
                log.error("Did not get any place photo!");
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting photo: {}", e.getMessage());
            return null;
        }
    }
}
