package com.backend.wanderverse_server.service;

import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.ImageResult;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.model.*;

public interface PlacesService {

    // Search for destinations using text
    // optional to specify locationBias:
    // -> Circle for radius, Rectangle for rectangle specified by 2 points (Northeast, SouthWest)
    // specify required data in includedTypes (refer to API Doc: https://developers.google.com/maps/documentation/places/web-service/text-search)
    PlacesSearchResult[] textSearch(String query, FindPlaceFromTextRequest.LocationBias locationBias, PlaceType... includedTypes);

    // search for destinations in a certain radius around the location
    PlacesSearchResult[] nearbySearch(LatLng location, int radius, PlaceType placeType);

    // get Place details about the destination
    PlaceDetails getPlaceDetails(String placeId, PlaceDetailsRequest.FieldMask... fieldMasks);

    ImageResult getPlacePhoto(String photoReference, Integer maxWidth, Integer maxHeight);
}
