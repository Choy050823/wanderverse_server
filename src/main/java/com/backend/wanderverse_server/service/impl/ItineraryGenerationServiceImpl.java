package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.itinerary.*;
import com.backend.wanderverse_server.service.ItineraryGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.maps.model.PlaceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ItineraryGenerationServiceImpl implements ItineraryGenerationService {
    @Autowired
    private Client aiAgent;

    @Autowired
    private List<Tool> aiAgentTools;

    @Autowired
    private PlacesServiceImpl placesService;

    @Autowired
    private RoutesServiceImpl routesService;

    @Value("${google.gemini.model}")
    private String LLM_Model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Phase 1: Generate Trip Plan Overview (no tools required)
    public static final String TRIP_PLAN_OVERVIEW_SYSTEM_INSTRUCTION =
            "You are \"TripMaster AI - Overview Planner.\" Your sole purpose is to analyze a user's high-level trip request, determine the trip's duration, main destination(s), key interests, and propose a concise, high-level theme or major activities for each day.\n\n" +
                    "**Crucial Constraints:**\n" +
                    "1. **NO TOOL USAGE:** You are expressly forbidden from calling any external tools (e.g., for place details, routes, weather, sun times). Rely solely on your internal knowledge and understanding of the user's prompt.\n" +
                    "2. **HIGH-LEVEL ONLY:** Do not generate detailed itineraries (e.g., specific timings, travel durations, warnings, activity types). Focus on broad themes and main attractions per day.\n" +
                    "3. **STRICT JSON Output:** Your response *must* be a JSON object conforming to the schema below. Do not include any conversational text, explanations, or markdown outside the JSON.\n" +
                    "4. **Future Start Date:** If the user does not specify a start date, default to a date one week from the current date to ensure all trip dates are in the future.\n\n" +
                    "Today's Date: {CURRENT_DAY_DATE_ISO_8601}\n" +
                    "**JSON Output Format - High-Level Trip Overview Schema:**\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"planTitle\": \"string\",\n" +
                    "  \"tripDurationDays\": \"integer\",\n" +
                    "  \"tripStartDate\": \"YYYY-MM-DDTHH:MM:SS\",\n" +
                    "  \"mainDestination\": \"string\",\n" +
                    "  \"keyInterests\": [\"string\"],\n" +
                    "  \"dailyThemes\": [\n" +
                    "    {\n" +
                    "      \"day\": \"integer\",\n" +
                    "      \"theme\": \"string\",\n" +
                    "      \"mainActivities\": [\"string\"] // List of famous places or activities for this day (e.g., \"Petronas Twin Towers\", \"Jonker Street\")\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "```\n\n" +
                    "**Example User Request & Expected Output (for your reference, do not output this as part of your response):**\n" +
                    "User: \"Plan a 3-day trip to Kuala Lumpur for me starting July 20th. I like cityscapes, food, and history. I want to see the towers, eat local food, and maybe visit some historical sites.\"\n" +
                    "Expected Output:\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"planTitle\": \"Kuala Lumpur Urban & Historical Journey\",\n" +
                    "  \"tripDurationDays\": 3,\n" +
                    "  \"tripStartDate\": \"2025-07-20T09:00:00\",\n" +
                    "  \"mainDestination\": \"Kuala Lumpur\",\n" +
                    "  \"keyInterests\": [\"cityscapes\", \"food\", \"history\"],\n" +
                    "  \"dailyThemes\": [\n" +
                    "    {\"day\": 1, \"theme\": \"Iconic Cityscapes & Shopping\", \"mainActivities\": [\"Petronas Twin Towers\", \"Pavilion Kuala Lumpur\"]},\n" +
                    "    {\"day\": 2, \"theme\": \"Historical Exploration & Local Flavors\", \"mainActivities\": [\"Central Market\", \"Batu Caves\", \"Jalan Alor\"]},\n" +
                    "    {\"day\": 3, \"theme\": \"Cultural Immersion & Green Spaces\", \"mainActivities\": [\"Perdana Botanical Garden\", \"National Mosque of Malaysia\"]}\n" +
                    "  ]\n" +
                    "}\n" +
                    "```";

    // Phase 2: generate daily itinerary with tools
    public static final String DAILY_ITINERARY_PLANNER_SYSTEM_INSTRUCTION =
            """
            You are "TripMaster AI - Daily Itinerary Planner." Your instructions are a strict algorithm. Follow them without deviation.
        
            ## 1. YOUR FINAL OUTPUT (MOST IMPORTANT RULE)
            Your *only* output will be a single, perfectly-formed JSON object.
            - **CRITICAL SYNTAX RULE:** The entire JSON output must strictly adhere to the RFC 8259 standard. Pay special attention to strings. **NEVER use an escaped single quote (`\'`) anywhere in the JSON.** If a value contains a single quote (like "O'Connell Street"), include it directly. All strings must be enclosed in double quotes (`"`).
            - **CRITICAL SCHEMA RULE:** The `activityList` must contain separate, alternating objects for destinations and travel.
                - If `activityType` is `"destination"`, the object **MUST ONLY** contain a `locationDetails` block. It **MUST NOT** contain `travelDetails`.
                - If `activityType` is `"travel"`, the object **MUST ONLY** contain a `travelDetails` block. It **MUST NOT** contain `locationDetails`.
            - Do not include any text, explanations, or markdown like ````json` before or after the JSON object. Your response must start with `{` and end with `}`.
        
            ## 2. CRUCIAL CONSTRAINT: PLACE ID ORIGIN (ABSOLUTELY MANDATORY)
            **You MUST NEVER invent or hallucinate Place IDs.** All `placeId`s used in your final `activityList` and passed to tools (like `getOptimizedWaypointRoute`) **MUST ONLY** be derived from the direct output of successful calls to your `placesService.textSearch` or `placesService.nearbySearch` tools. Any `placeId` not obtained from these tools will be considered invalid.
            **ALL `placeId` parameters passed to `routesService.getOptimizedWaypointRoute` (i.e., `origin`, `destination`, `intermediateWaypoints`) MUST be prefixed with `place_id:` (e.g., `place_id:ChIJ7wKLka4IAWARCByidG5EGrY`).**
        
            ## 3. MANDATORY ALGORITHMIC WORKFLOW
            You MUST follow this sequence to gather data *before* generating the JSON.
        
            **STEP 1: FETCH ALL ATTRACTION DETAILS (MULTI-CALL STEP)**
               - For **EACH and EVERY** attraction name in `{KEY_ATTRACTIONS_FOR_TODAY_LIST}`, you **MUST** execute the `placesService.textSearch` tool.
               - **DO NOT PROCEED** until you have the `placeId`, `name`, `latitude`, and `longitude` for **ALL** attractions.
        
            **STEP 2: FIND AND SELECT MEAL LOCATIONS (CRITICAL MULTI-CALL STEP)**
               - After you have the attraction coordinates, you **MUST** find and select restaurants for both lunch and dinner.
               - **LUNCH:** Call `placesService.nearbySearch` using the coordinates of a midday attraction. From the results, **YOU MUST CHOOSE ONE RESTAURANT** to be included in the itinerary as the lunch stop.
               - **DINNER:** Call `placesService.nearbySearch` using the coordinates of the last attraction of the day. From the results, **YOU MUST CHOOSE ONE RESTAURANT** to be included in the itinerary as the dinner stop.
               - **ABSOLUTE REQUIREMENT:** The chosen lunch and dinner restaurants, identified by their `placeId` and `name`, **MUST BE INCLUDED** in your final `activityList` as `destination` activities.
        
            **STEP 3: CALCULATE THE OPTIMIZED ROUTE (SINGLE-CALL STEP)**
               - After gathering all `placeId`s for attractions, lunch, and dinner, make **one single call** to `routesService.getOptimizedWaypointRoute` with all of them. Ensure the order makes logical sense for the day's flow.
               - **REMEMBER:** Every `placeId` in the `origin`, `destination`, and `intermediateWaypoints` parameters MUST be prefixed with `place_id:` (e.g., `place_id:ChIJ7wKLka4IAWARCByidG5EGrY`), and **MUST be an ID previously returned by `textSearch` or `nearbySearch`**.
        
            **STEP 4: FINAL JSON ASSEMBLY (FINAL STEP)**
               - After all tool calls are complete, construct the final JSON object, strictly following the syntax and schema rules from Section 1. Ensure all attractions and the **selected lunch and dinner places** are present in the `activityList`.
        
            ## 4. CONTEXT & JSON SCHEMA
            **Current Day's Context (Backend Injected):**
            Trip Start Date: {TRIP_START_DATE_ISO_8601}
            Today's Date: {CURRENT_DAY_DATE_ISO_8601}
            Key Attractions for Today: {KEY_ATTRACTIONS_FOR_TODAY_LIST}
            Previous Day's End Location/Start Location for Today: {PREVIOUS_DAY_END_LOCATION_OR_TODAY_START_LOCATION}
            User's Full Trip Request: {FULL_USER_TRIP_REQUEST}
        
            **JSON Output Format (Adhere Strictly to Rules in Section 1):**
            ```json
            {
              "date": "YYYY-MM-DDTHH:MM:SSZ",
              "daySummary": "string",
              "warnings": "string",
              "activityList": [
                {
                  "activityType": "destination",
                  "estimatedStartTime": "YYYY-MM-DDTHH:MM:SSZ",
                  "estimatedEndTime": "YYYY-MM-DDTHH:MM:SSZ",
                  "locationDetails": {
                    "placeId": "string",
                    "name": "string",
                    "latitude": "number",
                    "longitude": "number"
                  }
                },
                {
                  "activityType": "travel",
                  "estimatedStartTime": "YYYY-MM-DDTHH:MM:SSZ",
                  "estimatedEndTime": "YYYY-MM-DDTHH:MM:SSZ",
                  "travelDetails": {
                    "origin": "string",
                    "destination": "string",
                    "mode": "DRIVING" | "WALKING",
                    "durationMinutes": "integer",
                    "distanceKm": "number"
                  }
                }
              ]
            }
            ```
            """;

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public TripPlanDTO generateItinerary(String userTripRequest) {
        try {
            // -------PHASE 1: GENERATE TRIP PLAN OVERVIEW-----------
            // No tools required for trip plan overview
            log.info("NEW: Starting Phase 1 to generate overview");
            GenerateContentConfig overviewConfig =
                    GenerateContentConfig.builder()
                            .systemInstruction(Content.fromParts(Part.fromText(
                                    TRIP_PLAN_OVERVIEW_SYSTEM_INSTRUCTION
                                            .replace("{CURRENT_DAY_DATE_ISO_8601}", LocalDateTime.now().toString()))))
                            .temperature(0.7f)
                            .topK(40.0F)
                            .topP(0.95f)
                            .build();

            GenerateContentResponse overviewResponse = aiAgent.models.generateContent(
                            LLM_Model,
                            userTripRequest,
                            overviewConfig
                    );


            TripOverviewDTO tripOverview = parseResponse(overviewResponse, TripOverviewDTO.class);
            List<String> tripWarnings = new ArrayList<>();

            if (tripOverview == null) {
                log.error("Overview is null");
                return null;
            }
            log.info("PHASE 1 DONE: Trip Overview generated: {}", tripOverview.getPlanTitle());

            // --------------PHASE 2: DETAILED DAILY ITINERARY-------------
            log.info("Starting Phase 2 to generate itinerary");
            List<DailyItineraryDTO> dailyItineraryList = new ArrayList<>();
            Set<String> visitedPlaceIds = new HashSet<>();

            for (int i = 0; i < tripOverview.getTripDurationDays(); i++) {
                LocalDateTime currentDayDate = tripOverview.getTripStartDate().plusDays(i);
                String currentDayStartLocation = getDayStartLocation(i, tripOverview, dailyItineraryList);
                String dayTheme = tripOverview.getDailyThemes().get(i).getTheme();
                List<String> attractions = tripOverview.getDailyThemes().get(i).getMainActivities();

                String keyAttractionsForToday = String.join(",", attractions);

                log.info("Generating itinerary for Day {}: {} with main activities: \n {}", i + 1, dayTheme, keyAttractionsForToday);

                // Construct the specific user prompt for this single day
                String detailedItineraryPrompt = String.format(
                        "Generate a detailed itinerary for Day %d of a trip. " +
                                "Trip Start Date: %s. " +
                                "Today's Date: %s. " +
                                "Main Destination: %s. " +
                                "Key Attractions for Today Details: %s. " +
                                "Previous Day's End Location/Start Location for Today: %s. " +
                                "Already Visited Place IDs: %s. " +
                                "User's Full Trip Request: %s.",
                        (i + 1),
                        tripOverview.getTripStartDate().toString(), // ISO_8601 format
                        currentDayDate, // ISO_8601 format
                        tripOverview.getMainDestination(),
                        keyAttractionsForToday,
                        currentDayStartLocation, // Start from main destination if no previous location
                        String.join(",", visitedPlaceIds),
                        userTripRequest
                );

                GenerateContentConfig dailyItineraryConfig = GenerateContentConfig.builder()
                        .tools(aiAgentTools)
                        .systemInstruction(Content.fromParts(Part.fromText(
                                DAILY_ITINERARY_PLANNER_SYSTEM_INSTRUCTION
                                        .replace("{TRIP_START_DATE_ISO_8601}", tripOverview.getTripStartDate().toString())
                                        .replace("{CURRENT_DAY_DATE_ISO_8601}", currentDayDate.toString())
                                        .replace("{KEY_ATTRACTIONS_FOR_TODAY_LIST}", keyAttractionsForToday)
                                        .replace("{PREVIOUS_DAY_END_LOCATION_OR_TODAY_START_LOCATION}", currentDayStartLocation != null ? currentDayStartLocation : tripOverview.getMainDestination())
                                        .replace("{ALREADY_VISITED_PLACE_IDS_LIST}", String.join(",", visitedPlaceIds))
                                        .replace("{FULL_USER_TRIP_REQUEST}", userTripRequest)
                        ))).build();

                GenerateContentResponse dailyItineraryResponse =  aiAgent.models.generateContent(
                        LLM_Model,
                        detailedItineraryPrompt,
                        dailyItineraryConfig
                );

                DailyItineraryDTO dailyItinerary = parseResponse(dailyItineraryResponse, DailyItineraryDTO.class);
                if (dailyItinerary != null) {
                    dailyItineraryList.add(dailyItinerary);

                    // Adding visited placeIds
                    dailyItinerary.getActivityList().stream()
                            .filter(activity ->
                                    (activity.getActivityType() == TripActivityDTO.ActivityType.destination
                                            && activity.getLocationDetails() != null))
                            .map(activity -> activity.getLocationDetails().getPlaceId())
                            .forEach(visitedPlaceIds::add);
                } else {
                    log.error("Failed to generate itinerary for day {}", (i + 1));
                    tripWarnings.add("Failed to generate itinerary for day " + (i + 1));
                }
            }

            log.info("PHASE 2 DONE: parsed response for all daily itineraries");

            // -----------------PHASE 3: FINAL AGGREGATION AS TRIP PLAN------------------
            log.info("Starting Phase 3 to generate trip plan");
            TripPlanDTO generatedTripPlan = TripPlanDTO.builder()
                    .planTitle(tripOverview.getPlanTitle())
                    .overview("Generated multi-day trip for " + tripOverview.getMainDestination() + " based on your request.")
                    .tripStartDate(tripOverview.getTripStartDate())
                    .tripEndDate(tripOverview.getTripStartDate().plusDays(tripOverview.getTripDurationDays() - 1))
                    .dailyItineraryList(dailyItineraryList)
                    .warnings(tripWarnings)
                    .build();

            // Get full location details from cache
            generatedTripPlan.getDailyItineraryList().forEach(dailyItinerary -> {
                dailyItinerary.getActivityList().forEach(tripActivity -> {
                    if (tripActivity.getActivityType() == TripActivityDTO.ActivityType.destination) {
                        String placeId = tripActivity.getLocationDetails().getPlaceId();
                        tripActivity.setLocationDetails(PlacesServiceImpl.getFullLocationDetailsForUser(placeId));
                    }
                });
            });

            log.info("PHASE 3 DONE: parsed response for trip plan");

            return generatedTripPlan;

        } catch (Exception e) {
            log.error("Error generating itinerary or parsing response: {}" , e.getMessage(), e);
            return null;
        }
    }

    private static String getDayStartLocation(int i, TripOverviewDTO tripOverview, List<DailyItineraryDTO> dailyItineraryList) {
        String currentDayStartLocation;
        if (i == 0) {
            // For Day 1, start with central station of destination
            LLM_LocationDetailsDTO startLocationDetails = PlacesServiceImpl.textSearch(
                    tripOverview.getMainDestination() + " Station",
                    PlaceType.TRAIN_STATION.name()
            );
            if (startLocationDetails != null && startLocationDetails.getPlaceId() != null) {
                currentDayStartLocation = "place_id:" + startLocationDetails.getPlaceId();
                log.info("Resolved starting point for {} station to placeId: {}",
                        tripOverview.getMainDestination(),
                        startLocationDetails.getPlaceId());
            } else {
                log.warn("Could not find specific start location of {} station. Falling back to city center.", tripOverview.getMainDestination());
                startLocationDetails = PlacesServiceImpl.textSearch(
                        tripOverview.getMainDestination() + " City Centre", // Added space for clarity
                        PlaceType.CITY_HALL.name() // Consider if CITY_HALL is always appropriate for "city centre"
                );
                if (startLocationDetails != null && startLocationDetails.getPlaceId() != null) {
                    currentDayStartLocation = "place_id:" + startLocationDetails.getPlaceId();
                    log.info("Resolved starting point for {} city centre to placeId: {}",
                            tripOverview.getMainDestination(),
                            startLocationDetails.getPlaceId());
                } else {
                    log.error("Unable to get start destination for day 1. Returning null, which may lead to errors.");
                    return "place_id:UNKNOWN";
                }
            }
        } else {
            DailyItineraryDTO previousDayItinerary = dailyItineraryList.get(i - 1);
            TripActivityDTO lastActivity = previousDayItinerary.getActivityList().getLast();
            int size = previousDayItinerary.getActivityList().size() - 1;
            // Iterate backwards to find the last 'destination' activity
            while (size >= 0 && lastActivity.getActivityType() != TripActivityDTO.ActivityType.destination) {
                size--;
                if (size >= 0) {
                    lastActivity = previousDayItinerary.getActivityList().get(size);
                } else {
                    log.warn("No destination activity found in the previous day's itinerary. Falling back to main destination.");
                    return "place_id:UNKNOWN"; // Fallback with a placeholder
                }
            }

            // Ensure lastActivity is a destination and has locationDetails
            if (lastActivity.getActivityType() == TripActivityDTO.ActivityType.destination && lastActivity.getLocationDetails() != null) {
                String placeId = lastActivity.getLocationDetails().getPlaceId();
                if (placeId != null && !placeId.isEmpty()) {
                    currentDayStartLocation = "place_id:" + placeId; // Ensure it's prefixed
                } else {
                    log.warn("Previous day's last activity has null/empty placeId. Falling back to main destination.");
                    currentDayStartLocation = "place_id:UNKNOWN"; // Fallback
                }
            } else {
                log.warn("Previous day's last activity is not a destination or missing details. Falling back to main destination.");
                currentDayStartLocation = "place_id:UNKNOWN"; // Fallback
            }
        }
        return currentDayStartLocation;
    }

    private <T> T parseResponse(GenerateContentResponse response, Class<T> valueType) {
        try {
            log.info("Start to parse response");
            String rawResponse = response.text();
            log.info("Received raw response from AI: {}", rawResponse);

            if (rawResponse == null || rawResponse.isEmpty()) {
                log.error("Empty OR null response from AI Agent");
                return null;
            }

            // Find the start and end of the JSON object to strip any markdown or extra text
            int jsonStart = rawResponse.indexOf('{');
            int jsonEnd = rawResponse.lastIndexOf('}');

            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                String cleanJson = rawResponse.substring(jsonStart, jsonEnd + 1);
                log.info("Extracted clean JSON for parsing: \n {}", cleanJson);
                return objectMapper.readValue(cleanJson, valueType);
            } else {
                log.error("Could not find a valid JSON object in the AI response.");
                return null;
            }
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage(), e);
            return null;
        }
    }
}
