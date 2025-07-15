package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.itinerary.DailyItineraryDTO;
import com.backend.wanderverse_server.model.dto.itinerary.TripActivityDTO;
import com.backend.wanderverse_server.model.dto.itinerary.TripOverviewDTO;
import com.backend.wanderverse_server.model.dto.itinerary.TripPlanDTO;
import com.backend.wanderverse_server.service.ItineraryGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.genai.Client;
import com.google.genai.types.*;
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

    @Value("${google.gemini.model}")
    private String LLM_Model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Phase 1 System Instruction: Generate Trip Plan Overview (no tools required)
    public static final String TRIP_PLAN_OVERVIEW_SYSTEM_INSTRUCTION =
            "You are \"TripMaster AI - Overview Planner.\" Your sole purpose is to analyze a user's high-level trip request, determine the trip's duration, main destination(s), key interests, and propose a concise, high-level theme or major activities for each day.\n\n" +
                    "**Crucial Constraints:**\n" +
                    "1.  **NO TOOL USAGE:** You are expressly forbidden from calling any external tools (e.g., for place details, routes, weather, sun times). Rely solely on your internal knowledge and understanding of the user's prompt.\n" +
                    "2.  **HIGH-LEVEL ONLY:** Do not generate detailed itineraries (e.g., specific timings, travel durations, warnings, activity types). Focus on broad themes and main attractions per day.\n" +
                    "3.  **STRICT JSON Output:** Your response *must* be a JSON object conforming to the schema below. Do not include any conversational text, explanations, or markdown outside the JSON.\n\n" +
                    "**JSON Output Format - High-Level Trip Overview Schema:**\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"planTitle\": \"string\",\n" +
                    "  \"tripDurationDays\": \"integer\",\n" +
                    "  \"tripStartDate\": \"YYYY-MM-DDTHH:MM:SS\",\n" + // Added tripStartDate
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
                    "  \"tripStartDate\": \"2025-07-20T09:00:00\",\n" + // Added tripStartDate to example
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
            "You are \"TripMaster AI - Daily Itinerary Planner.\" Your task is to generate a detailed and optimized itinerary for a *single specific day* of a multi-day trip. You will use provided external tools to gather precise data and make informed decisions.\n\n" +
                    "**Your Core Workflow Principles:**\n" +
                    "1.  **Context Integration:** Always integrate the provided overall trip context, specific daily details, and already visited places into your planning.\n" +
                    "2.  **Tool First, Then Reason:** Whenever factual data is required (like coordinates, travel times, distances, or specific place details), prioritize calling the appropriate API tool. Do not hallucinate or guess.\n" +
                    "3.  **Tool Usage Guidance (IMPORTANT):**\n" +
                    "    * Use `textSearch(query: string, placeType: string)` to get `placeId` and initial details for named locations (e.g., \"Eiffel Tower\").\n" +
                    "    * Use `nearbySearch(locationLat: number, locationLong: number, radius: number, placeType: string)` to find places around a coordinate (e.g., \"restaurants near KLCC\").\n" +
                    "    * **Crucial Note:** The `LocationDetailsDTO` returned by `textSearch` and `nearbySearch` will contain `locationUrl` (a public Google Maps URL for the place) and `locationImage` (a byte array for a photo). When populating the `locationUrl` field in the final JSON, use the `locationUrl` from the tool's response. For `locationImageUrl`, if a direct public image URL is not explicitly provided by a tool, leave this field as `null` or a placeholder, as you cannot convert `byte[]` to a URL.\n" +
                    "    * Use `getOptimizedWaypointRoute(origin: string, destination: string, intermediateWaypoints: string, travelMode: string, departureTime: string)` for segments where you need to visit multiple intermediate places in the most efficient order. Provide `departureTime` in ISO 8601 format (e.g., '2025-07-12T09:00:00Z').\n" +
                    "    * Use `getFixedWaypointRoute(origin: string, destination: string, fixedIntermediateWaypoints: string, travelMode: string, departureTime: string)` for segments where waypoints must be visited in a specific, fixed order. Provide `departureTime` in ISO 8601 format.\n" +
                    "    * Use `getRouteWithAvoidance(origin: string, destination: string, travelMode: string, avoidTolls: boolean, avoidHighways: boolean, avoidFerries: boolean, departureTime: string)` if the user specified travel restrictions. Provide `departureTime` in ISO 8601 format.\n" +
                    "    * **Allowed `travelMode` values are: \"DRIVING\", \"WALKING\", \"BICYCLING\", \"TRANSIT\".**\n" +
                    "    * Remember to use `departureTime` (as `Instant` ISO 8601 string) for all route calculations.\n" +
                    "4.  **Reasoning & Optimization:**\n" +
                    "    * Identify and respect time constraints (e.g., \"around sunset\"). If a fixed time makes a route impossible, add a clear warning in the `warnings` field.\n" +
                    "    * Estimate realistic activity durations for each destination (e.g., 2 hours for a museum, 1 hour for a quick photo stop).\n" +
                    "    * Calculate `estimatedStartTime` and `estimatedEndTime` for each activity (destination or travel) in HH:MM format (e.g., \"09:00\", \"15:30\").\n" +
                    "    * Ensure logical flow and minimized travel time for the day's activities.\n" +
                    "    * The `locationUrl` should be set to the Google Maps URL provided by the `textSearch` or `nearbySearch` tool.\n" +
                    "    * The `activityList` last element `activityType` field should be set to \"destination\" not \"travel\".\n" +
                    "5.  **Strict JSON Output:** Your final response *must* be a JSON object representing the `DailyItineraryDTO` for the current day, adhering strictly to the specified schema. Do not include any conversational text or explanation outside the JSON.\n\n" +
                    "**Current Day's Context (Backend Injected - You MUST use this information):**\n" +
                    "Trip Start Date: {TRIP_START_DATE_ISO_8601}\n" +
                    "Today's Date: {CURRENT_DAY_DATE_ISO_8601}\n" +
                    "Key Attractions for Today: {KEY_ATTRACTIONS_FOR_TODAY_LIST} (e.g., \"Petronas Twin Towers, Central Market\")\n" +
                    "Previous Day's End Location/Start Location for Today: {PREVIOUS_DAY_END_LOCATION_OR_TODAY_START_LOCATION}\n" +
                    "Already Visited Place IDs (DO NOT include these in today's plan unless explicitly requested or for route purposes): {ALREADY_VISITED_PLACE_IDS_LIST}\n" +
                    "User's Full Trip Request (for overall context): {FULL_USER_TRIP_REQUEST}\n\n" +
                    "**JSON Output Format - `DailyItineraryDTO` Schema (YOU MUST ADHERE TO THIS SCHEMA):**\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"date\": \"YYYY-MM-DDTHH:MM:SS\",\n" +
                    "  \"daySummary\": \"string\",\n" +
                    "  \"warnings\": \"string\",\n" +
                    "  \"activityList\": [\n" +
                    "    {\n" +
                    "      \"activityType\": \"destination\" | \"travel\",\n" +
                    "      \"estimatedStartTime\": \"YYYY-MM-DDTHH:MM:SS\",\n" +
                    "      \"estimatedEndTime\": \"YYYY-MM-DDTHH:MM:SS\",\n" +
                    "      \"locationDetails\": {\n" +
                    "        \"placeId\": \"string\",\n" +
                    "        \"name\": \"string\",\n" +
//                    "        \"editorialSummary\": \"string\",\n" +
//                    "        \"formattedAddress\": \"string\",\n" +
//                    "        \"openingHours\": \"string\",\n" +
//                    "        \"rating\": \"number\",\n" +
//                    "        \"website\": \"string\",\n" +
//                    "        \"phoneNumber\": \"string\",\n" +
//                    "        \"locationUrl\": \"string\",\n" +
//                    "        \"locationImageUrl\": \"string\" // Populate with locationUrl if no specific image URL is available from tools, or leave null\n" +
                    "      },\n" +
                    "      \"travelDetails\": {\n" +
                    "        \"origin\": \"string\",\n" +
                    "        \"destination\": \"string\",\n" +
                    "        \"mode\": \"DRIVING\" | \"WALKING\" | \"BICYCLING\" | \"TRANSIT\",\n" +
                    "        \"durationMinutes\": \"integer\",\n" +
                    "        \"distanceKm\": \"number\"\n" +
                    "      },\n" +
//                    "      \"notesList\": [\"string\"]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "```\n" +
                    "Remember: If `activityType` is \"destination\", only populate `locationDetails`. If `activityType` is \"travel\", only populate `travelDetails`.\n" +
                    "After completing all necessary reasoning and tool calls for this *single day*, synthesize the plan and output the final itinerary in a single JSON object.";

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public TripPlanDTO generateItinerary(String userTripRequest) {
        try {
            // -------PHASE 1: GENERATE TRIP PLAN OVERVIEW-----------
            // No tools required for trip plan overview
            GenerateContentConfig overviewConfig =
                    GenerateContentConfig.builder()
                            .systemInstruction(Content.fromParts(Part.fromText(TRIP_PLAN_OVERVIEW_SYSTEM_INSTRUCTION)))
                            .temperature(0.7f)
                            .topK(40.0F)
                            .topP(0.95f)
                            .build();

            GenerateContentResponse overviewResponse = aiAgent.models.generateContent(
                            LLM_Model,
                            userTripRequest,
                            overviewConfig
                    );

            log.info("PHASE 1 DONE");

            TripOverviewDTO tripOverview = parseResponse(overviewResponse, TripOverviewDTO.class);
            List<String> tripWarnings = new ArrayList<>();

            if (tripOverview == null) {
                log.error("Overview is null");
                return null;
            }

            // --------------PHASE 2: DETAILED DAILY ITINERARY-------------
            List<DailyItineraryDTO> dailyItineraryList = new ArrayList<>();
            Set<String> visitedPlaceIds = new HashSet<>();

            for (int i = 0; i < tripOverview.getTripDurationDays(); i++) {
                LocalDateTime currentDayDate = tripOverview.getTripStartDate().plusDays(i);

                String currentDayStartLocation;
                if (i == 0) {
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

                String keyAttractionsForToday = String.join(",", tripOverview.getDailyThemes().get(i).getMainActivities());

                // Construct the specific user prompt for this single day
                String detailedItineraryPrompt = String.format(
                        "Generate a detailed itinerary for Day %d of a trip. " +
                                "Trip Start Date: %s. " +
                                "Today's Date: %s. " +
                                "Main Destination: %s. " +
                                "Key Attractions for Today: %s. " +
                                "Previous Day's End Location/Start Location for Today: %s. " +
                                "Already Visited Place IDs: %s. " +
                                "User's Full Trip Request: %s.",
                        (i + 1),
                        tripOverview.getTripStartDate().toString(), // ISO_8601 format
                        currentDayDate.toString(), // ISO_8601 format
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
                                    activity.getActivityType() == TripActivityDTO.ActivityType.destination
                                            && activity.getLocationDetails() != null)
                            .map(activity -> activity.getLocationDetails().getPlaceId())
                            .forEach(visitedPlaceIds::add);
                } else {
                    log.error("Failed to generate itinerary for day {}", i + 1);
                    // add warning to trip overview later
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
            log.error("Error generating itinerary or parsing response: {}" , e.getMessage());
            return null;
        }
    }

    private <T> T parseResponse(GenerateContentResponse response, Class<T> valueType) {
        try {
            log.info("Start to parse response");
            String rawResponse = response.text();
//            log.info("Received raw response from AI: {}", rawResponse);

            if (rawResponse == null || rawResponse.isEmpty()) {
                log.error("Empty OR null response from AI Agent");
                return null;
            }

            // Find the start and end of the JSON object to strip any markdown or extra text
            int jsonStart = rawResponse.indexOf('{');
            int jsonEnd = rawResponse.lastIndexOf('}');

            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                String cleanJson = rawResponse.substring(jsonStart, jsonEnd + 1);
                log.info("Extracted clean JSON for parsing: {}", cleanJson);
//                if (response.automaticFunctionCallingHistory().isPresent()) {
//                    for (Content content : response.automaticFunctionCallingHistory().get()) {
//                        log.info("Function Call: {}", content.text());
//                    }
//                }
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
