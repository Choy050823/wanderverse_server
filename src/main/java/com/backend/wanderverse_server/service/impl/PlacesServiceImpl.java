package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.service.PlacesService;
import com.google.maps.*;
import com.google.maps.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PlacesServiceImpl implements PlacesService {
    @Autowired
    private GeoApiContext geoApiContext;

    @Override
    public PlacesSearchResult[] textSearch(String query, FindPlaceFromTextRequest.LocationBias locationBias, PlaceType... includedTypes) {
        try {
            TextSearchRequest request = PlacesApi
                    .textSearchQuery(geoApiContext, query)
                    .language("en");

//            if (locationBias != null) {
//                request.(locationBias);
//            }

            if (includedTypes != null && includedTypes.length > 0) {
                request.type(includedTypes[0]);
            }

            return request.await().results;
        } catch (Exception e) {
            log.error("Unexpected error occurred during text search: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public PlacesSearchResult[] nearbySearch(LatLng location, int radius, PlaceType placeType) {
        try {
            NearbySearchRequest request = PlacesApi
                    .nearbySearchQuery(geoApiContext, location)
                    .radius(radius)
                    .language("en");

            if (placeType != null) {
                request.type(placeType);
            }
            return request.await().results;
        } catch (Exception e) {
            log.error("Unexpected error occurred during nearby search: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public PlaceDetails getPlaceDetails(String placeId, PlaceDetailsRequest.FieldMask... fieldMasks) {
        try {
            PlaceDetailsRequest request = PlacesApi
                    .placeDetails(geoApiContext, placeId)
                    .language("en");

            if (fieldMasks != null && fieldMasks.length > 0) {
                request.fields(fieldMasks);
            }

            return request.await();
        } catch (Exception e) {
            log.error("Error getting Place Details: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ImageResult getPlacePhoto(String photoReference, Integer maxWidth, Integer maxHeight) {
        try {
            PhotoRequest request = PlacesApi
                    .photo(geoApiContext, photoReference)
                    .maxHeight(maxHeight)
                    .maxWidth(maxWidth);

            return request.await();
        } catch (Exception e) {
            log.error("Error getting photo: {}", e.getMessage());
            return null;
        }
    }
}
