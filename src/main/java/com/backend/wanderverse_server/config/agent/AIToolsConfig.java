package com.backend.wanderverse_server.config.agent;

import com.backend.wanderverse_server.service.impl.PlacesServiceImpl;
import com.backend.wanderverse_server.service.impl.RoutesServiceImpl;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.List;

@Configuration
@Slf4j
public class AIToolsConfig {

    @Bean
    public List<Tool> aiAgentTools() {
        try {
            // Places Service Methods
            Method textSearchMethod = PlacesServiceImpl.class.getMethod("textSearch", String.class, String.class);
            Method nearbySearchMethod = PlacesServiceImpl.class.getMethod("nearbySearch", double.class, double.class, int.class, String.class);

            // Routes Service Methods
//            Method getFixedWaypointRouteMethod = RoutesServiceImpl.class.getMethod("getFixedWaypointRoute", String.class, String.class, String.class, String.class, String.class);
            Method getOptimizedWaypointRouteMethod = RoutesServiceImpl.class.getMethod("getOptimizedWaypointRoute", String.class, String.class, String.class, String.class, String.class);
//            Method getRouteWithAvoidanceMethod = RoutesServiceImpl.class.getMethod("getRouteWithAvoidance", String.class, String.class, String.class, boolean.class, boolean.class, boolean.class, String.class);

            return ImmutableList.of(
                    Tool.builder()
                            .functions(
                                    textSearchMethod,
                                    nearbySearchMethod,
//                                    getFixedWaypointRouteMethod,
                                    getOptimizedWaypointRouteMethod
//                                    getRouteWithAvoidanceMethod
                            ).build()
            );
        } catch (Exception e) {
            log.error("Error reflecting tool methods for Gemini SDK: {}", e.getMessage());
            // Depending on your application, you might want to throw a RuntimeException
            // or handle gracefully if tools are optional.
            throw new RuntimeException("Failed to configure Gemini tools due to missing methods.", e);
        }
    }
}
