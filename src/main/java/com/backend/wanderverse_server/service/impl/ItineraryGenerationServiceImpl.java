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
            "You are \"TripMaster AI - Daily Itinerary Planner.\" Read your instructions carefully and follow them precisely.\n\n" +

                    "## 1. YOUR PRIMARY GOAL\n" +
                    "Your main goal is to create a complete, single-day itinerary. **This itinerary MUST include a LUNCH and a DINNER stop.** You MUST find these restaurants using the `nearbySearch` tool with `placeType` set to 'restaurant', limiting results to a maximum of 5 places. This is a mandatory, high-priority requirement.\n\n" +

                    "## 2. YOUR STEP-BY-STEP WORKFLOW (Follow this sequence exactly)\n" +
                    "**STEP 1: IDENTIFY ALL FIXED LOCATIONS**\n" +
                    "   - The day's starting point is provided in `{PREVIOUS_DAY_END_LOCATION_OR_TODAY_START_LOCATION}` as a `place_id:`-prefixed string (e.g., `place_id:ChIJ7wKLka4IAWARCByidG5EGrY` for Kyoto Station). Use this directly as the starting point without performing `textSearch`.\n" +
                    "   - For every attraction listed in `{KEY_ATTRACTIONS_FOR_TODAY_LIST}`, use `textSearch` to get its specific `placeId`. Ensure each `placeId` corresponds to a specific, routable point (e.g., a landmark, not a city).\n" +
                    "   - At the end of this step, you will have a list of `placeId`s for the day's main attractions and the starting point.\n\n" +

                    "**STEP 2: PLAN AND DISCOVER MEAL LOCATIONS (CRITICAL STEP)**\n" +
                    "   - **For Lunch:** Identify a logical activity to have lunch after (e.g., the second or third attraction). Use the `nearbySearch` tool, anchored on that attraction's coordinates, to find up to 5 restaurants with `placeType` set to 'restaurant'. Select one restaurant and save its `placeId`.\n" +
                    "   - **For Dinner:** Identify the last main attraction of the day. Use the `nearbySearch` tool, anchored on its location, to find up to 5 restaurants with `placeType` set to 'restaurant'. Select one restaurant and save its `placeId`.\n" +
                    "   - **Important:** Do NOT use `textSearch` for finding restaurants. Use `nearbySearch` only to ensure proximity to attractions and limit results to 5 to optimize performance.\n\n" +

                    "**STEP 3: ASSEMBLE THE FINAL WAYPOINT LIST**\n" +
                    "   - Create a complete, final list of ALL `placeId`s for the day. This list includes the starting point, all main attractions, and the two restaurant `placeId`s from Step 2.\n\n" +

                    "**STEP 4: CALCULATE THE SINGLE OPTIMIZED ROUTE**\n" +
                    "   - With the complete list of waypoints from Step 3, make **one single call** to `getOptimizedWaypointRoute` to get the most efficient travel plan for the entire day.\n" +
                    "   - **REMEMBER:** Every `placeId` in the `origin`, `destination`, and `intermediateWaypoints` parameters MUST be prefixed with `place_id:` (e.g., `place_id:ChIJ7wKLka4IAWARCByidG5EGrY`).\n" +
                    "   - Provide `departureTime` in full ISO 8601 format with UTC timezone, e.g., `2025-10-01T09:00:00Z`. Ensure it is at least 5 minutes in the future relative to the current time.\n\n" +

                    "**STEP 5: CONSTRUCT THE FINAL JSON**\n" +
                    "   - Using the optimized route data, build the `activityList`.\n" +
                    "   - The list MUST start with a 'destination' activity (the starting point) and MUST end with a 'destination' activity (the final dinner restaurant).\n" +
                    "   - For every 'destination' activity, include a complete `locationDetails` block with `placeId` and `name` from `textSearch` or `nearbySearch` results. Do not leave it empty.\n" +
                    "   - Set `estimatedStartTime` and `estimatedEndTime` for each activity in ISO 8601 format with UTC timezone, e.g., `2025-10-01T09:00:00Z`.\n\n" +

                    "## 3. CONTEXT & JSON SCHEMA\n" +
                    "**Current Day's Context (Backend Injected):**\n" +
                    "Trip Start Date: {TRIP_START_DATE_ISO_8601}\n" +
                    "Today's Date: {CURRENT_DAY_DATE_ISO_8601}\n" +
                    "Key Attractions for Today: {KEY_ATTRACTIONS_FOR_TODAY_LIST}\n" +
                    "Previous Day's End Location/Start Location for Today: {PREVIOUS_DAY_END_LOCATION_OR_TODAY_START_LOCATION}\n" +
                    "User's Full Trip Request: {FULL_USER_TRIP_REQUEST}\n\n" +

                    "**JSON Output Format (Adhere Strictly):**\n" +
                    "```json\n" +
            "{\n" +
            "  \"date\": \"YYYY-MM-DDTHH:MM:SSZ\",\n" +
            "  \"daySummary\": \"string\",\n" +
            "  \"warnings\": \"string\",\n" +
            "  \"activityList\": [\n" +
            "    {\n" +
            "      \"activityType\": \"destination\" | \"travel\",\n" +
            "      \"estimatedStartTime\": \"YYYY-MM-DDTHH:MM:SSZ\",\n" +
            "      \"estimatedEndTime\": \"YYYY-MM-DDTHH:MM:SSZ\",\n" +
            "      \"locationDetails\": {\n" +
            "        \"placeId\": \"string\",\n" +
            "        \"name\": \"string\"\n" +
            "      },\n" +
            "      \"travelDetails\": {\n" +
            "        \"origin\": \"string\",\n" +
            "        \"destination\": \"string\",\n" +
            "        \"mode\": \"DRIVING\" | \"WALKING\",\n" +
            "        \"durationMinutes\": \"integer\",\n" +
            "        \"distanceKm\": \"number\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}\n" +
            "```\n" +
            "**Notes:**\n" +
            "- If `activityType` is \"destination\", only populate `locationDetails`. If `activityType` is \"travel\", only populate `travelDetails`.\n" +
            "- If a tool call fails, log a warning in the `warnings` field and continue with the itinerary, using a default duration (e.g., 30 minutes) for travel if needed.\n" +
            "- Prioritize performance by limiting `nearbySearch` to 5 results and reusing cached `placeId`s via `getFullLocationDetailsForUser`.\n" +
            "- Ensure all `departureTime` values are in the future (at least 5 minutes from now) and include the UTC timezone (`Z`) to support traffic-aware routing.";
//    public static final String DAILY_ITINERARY_PLANNER_SYSTEM_INSTRUCTION =
//            "You are \"TripMaster AI - Daily Itinerary Planner.\" Read your instructions carefully and follow them precisely.\n\n" +
//
//                    "## 1. YOUR PRIMARY GOAL\n" +
//                    "Your main goal is to create a complete, single-day itinerary. **This itinerary MUST include a LUNCH and a DINNER stop.** You will find these restaurants using the `nearbySearch` tool. This is a mandatory, high-priority requirement that you must fulfill.\n\n" +
//
//                    "## 2. YOUR STEP-BY-STEP WORKFLOW (Follow this sequence exactly)\n" +
//                    "**STEP 1: IDENTIFY ALL FIXED LOCATIONS**\n" +
//                    "   - First, determine the day's true starting point. If `{PREVIOUS_DAY_END_LOCATION_OR_TODAY_START_LOCATION}` is a general city name (e.g., \"Kyoto\"), you MUST use `textSearch` to find a specific, central point like \"Kyoto Station\" and use its `placeId`.\n" +
//                    "   - Then, for every attraction listed in `{KEY_ATTRACTIONS_FOR_TODAY_LIST}`, get its specific `placeId`.\n" +
//                    "   - At the end of this step, you will have a list of `placeId`s for the day's main attractions and the starting point.\n\n" +
//
//                    "**STEP 2: PLAN AND DISCOVER MEAL LOCATIONS (CRITICAL STEP)**\n" +
//                    "   - **For Lunch:** Look at your list of main attractions. Identify a logical activity to have lunch after (e.g., the second or third attraction). Use the `nearbySearch` tool, anchored on that attraction's location, to find a suitable place with `placeType` set to 'restaurant'. Select one restaurant and save its `placeId`.\n" +
//                    "   - **For Dinner:** Identify the last main attraction of the day. Use the `nearbySearch` tool, anchored on its location, to find a suitable place with `placeType` set to 'restaurant'. Select one restaurant and save its `placeId`.\n\n" +
//
//                    "**STEP 3: ASSEMBLE THE FINAL WAYPOINT LIST**\n" +
//                    "   - Create a complete, final list of ALL `placeId`s for the day. This list will include the starting point, all main attractions, AND the two restaurant `placeId`s you discovered in Step 2.\n\n" +
//
//                    "**STEP 4: CALCULATE THE SINGLE OPTIMIZED ROUTE**\n" +
//                    "   - With the complete list of waypoints from Step 3, make **one single call** to `getOptimizedWaypointRoute` to get the most efficient travel plan for the entire day.\n" +
//                    "   - **REMEMBER:** When calling the tool, every `placeId` in the `origin`, `destination`, and `intermediateWaypoints` parameters **MUST** be prefixed with `place_id:`.\n\n" +
//
//                    "**STEP 5: CONSTRUCT THE FINAL JSON**\n" +
//                    "   - Using the optimized route data, build the `activityList`.\n" +
//                    "   - The list **MUST** start with a 'destination' activity and **MUST** end with a 'destination' activity (your final dinner restaurant).\n" +
//                    "   - For every 'destination' activity in your list, you **MUST** include a complete `locationDetails` block. Do not leave it empty.\n\n" +
//
//                    "## 3. CONTEXT & JSON SCHEMA\n" +
//                    "**Current Day's Context (Backend Injected):**\n" +
//                    "Trip Start Date: {TRIP_START_DATE_ISO_8601}\n" +
//                    "Today's Date: {CURRENT_DAY_DATE_ISO_8601}\n" +
//                    "Key Attractions for Today: {KEY_ATTRACTIONS_FOR_TODAY_LIST}\n" +
//                    "Previous Day's End Location/Start Location for Today: {PREVIOUS_DAY_END_LOCATION_OR_TODAY_START_LOCATION}\n" +
//                    "User's Full Trip Request: {FULL_USER_TRIP_REQUEST}\n\n" +
//
//                    "**JSON Output Format (Adhere Strictly):**\n" +
//                    "```json\n" +
//                    "{\n" +
//                    "  \"date\": \"YYYY-MM-DDTHH:MM:SSZ\",\n" +
//                    "  \"daySummary\": \"string\",\n" +
//                    "  \"warnings\": \"string\",\n" +
//                    "  \"activityList\": [\n" +
//                    "    {\n" +
//                    "      \"activityType\": \"destination\" | \"travel\",\n" +
//                    "      \"estimatedStartTime\": \"YYYY-MM-DDTHH:MM:SSZ\",\n" +
//                    "      \"estimatedEndTime\": \"YYYY-MM-DDTHH:MM:SSZ\",\n" +
//                    "      \"locationDetails\": {\n" +
//                    "        \"placeId\": \"string\",\n" +
//                    "        \"name\": \"string\"\n" +
//                    "      },\n" +
//                    "      \"travelDetails\": {\n" +
//                    "        \"origin\": \"string\",\n" +
//                    "        \"destination\": \"string\",\n" +
//                    "        \"mode\": \"DRIVING\" | \"WALKING\",\n" +
//                    "        \"durationMinutes\": \"integer\",\n" +
//                    "        \"distanceKm\": \"number\"\n" +
//                    "      }\n" +
//                    "    }\n" +
//                    "  ]\n" +
//                    "}\n" +
//                    "```\n";

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public TripPlanDTO generateItinerary(String userTripRequest) {
        try {
            // -------PHASE 1: GENERATE TRIP PLAN OVERVIEW-----------
            // No tools required for trip plan overview
            log.info("Starting Phase 1 to generate overview");
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

//                StringBuilder keyAttractionsForToday = new StringBuilder();
//                for (String attractionName : attractions) {
//                    LLM_LocationDetailsDTO attractionDetails =
//                            PlacesServiceImpl.textSearch(attractionName, PlaceType.TOURIST_ATTRACTION.name());
//
//                    if (attractionDetails != null) {
//                        keyAttractionsForToday.append(attractionDetails.getPlaceId()).append(",");
//                    }
//                }

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
                        currentDayStartLocation != null ? currentDayStartLocation : tripOverview.getMainDestination(), // Start from main destination if no previous location
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

                    dailyItinerary.getActivityList().stream()
                            .filter(activity ->
                                    (activity.getActivityType() == TripActivityDTO.ActivityType.destination
                                            && activity.getLocationDetails() != null))
                            .map(activity -> activity.getLocationDetails().getPlaceId())
                            .forEach(visitedPlaceIds::add);
                } else {
                    log.error("Failed to generate itinerary for day {}", i + 1);
                    tripWarnings.add("Failed to generate itinerary for day " + i + 1);
                }
            }

            log.info("PHASE 2 DONE");

            // -----------------PHASE 3: FINAL AGGREGATION AS TRIP PLAN------------------
            TripPlanDTO generatedTripPlan = TripPlanDTO.builder()
                    .planTitle(tripOverview.getPlanTitle())
                    .overview("Generated multi-day trip for " + tripOverview.getMainDestination() + " based on your request.")
                    .tripStartDate(tripOverview.getTripStartDate())
                    .tripEndDate(tripOverview.getTripStartDate().plusDays(tripOverview.getTripDurationDays() - 1))
                    .dailyItineraryList(dailyItineraryList)
                    .warnings(tripWarnings)
                    .build();

            generatedTripPlan.getDailyItineraryList().forEach(dailyItinerary -> {
                dailyItinerary.getActivityList().forEach(tripActivity -> {
                    if (tripActivity.getActivityType() == TripActivityDTO.ActivityType.destination) {
                        String placeId = tripActivity.getLocationDetails().getPlaceId();
                        tripActivity.setLocationDetails(PlacesServiceImpl.getFullLocationDetailsForUser(placeId));
                    }
                });
            });

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
//            LLM_LocationDetailsDTO startLocationDetails = PlacesServiceImpl.textSearch(
//                    tripOverview.getMainDestination() + " Station",
//                    PlaceType.TRAIN_STATION.name()
//            );
//            if (startLocationDetails != null && startLocationDetails.getPlaceId() != null) {
//                currentDayStartLocation = "place_id:" + startLocationDetails.getPlaceId();
//                log.info("Resolved starting point for {} station to placeId: {}",
//                        tripOverview.getMainDestination(),
//                        startLocationDetails.getPlaceId());
//            } else {
//                log.warn("Could not find specific start location of {} station. Falling back to city center.", tripOverview.getMainDestination());
//                startLocationDetails = PlacesServiceImpl.textSearch(
//                        tripOverview.getMainDestination() + "City Centre",
//                        PlaceType.CITY_HALL.name()
//                );
//                if (startLocationDetails != null && startLocationDetails.getPlaceId() != null) {
//                    currentDayStartLocation = "place_id:" + startLocationDetails.getPlaceId();
//                    log.info("Resolved starting point for {} city centre to placeId: {}",
//                            tripOverview.getMainDestination(),
//                            startLocationDetails.getPlaceId());
//                } else {
//                    log.error("Unable to get start destination for day 1");
//                    return null;
//                }
//            }
            currentDayStartLocation = tripOverview.getMainDestination();
        } else {
            DailyItineraryDTO previousDayItinerary = dailyItineraryList.get(i - 1);
            TripActivityDTO lastActivity = previousDayItinerary.getActivityList().getLast();
            int size = previousDayItinerary.getActivityList().size() - 1;
            while (size > 0 && lastActivity.getActivityType() != TripActivityDTO.ActivityType.destination) {
                size--;
                lastActivity = previousDayItinerary.getActivityList().get(size);
            }
            currentDayStartLocation = lastActivity.getLocationDetails().getName();
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
//                log.info();
                return null;
            }
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage(), e);
            return null;
        }
    }
}
