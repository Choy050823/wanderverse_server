package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.service.EmbeddingService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    @Value("${gemini.api.key}")
    private String googleApiKey;

    @Value("${gemini.embedding.model.name}")
    private String embeddingModelName;

    @Value("${qdrant.embedding.dimension}")
    private int embeddingDimension;

    @Autowired
    private WebClient embeddingModelWebClient;

    @Override
    public List<Float> getEmbeddings(String text, String taskType) {
        if (text == null || text.isEmpty()) {
          log.warn("Cannot generate embedding for empty or null text");
          return null;
        }

        if (taskType == null || taskType.trim().isEmpty()) {
            log.warn("TaskType cannot be empty. Using default or consider optional");
        }

        // Retry 3-times mechanism
        int maxRetries = 3;
        long delayMillis = 1000;
        Map<String, Object> requestBody = Map.of(
                "model", "models/" + embeddingModelName,
                "content", Map.of(
                        "parts", List.of(
                                Map.of("text", text)
                        )
                ),
                "taskType", taskType == null ? "SEMANTIC_SIMILARITY" : taskType
        );

        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                @SuppressWarnings("rawtypes")
                Mono<Map> responseMono = embeddingModelWebClient.post()
                        .uri(uriBuilder -> uriBuilder.pathSegment(
                                "v1beta", "models", embeddingModelName +":embedContent"
                        ).build())
                        .header("x-goog-api-key", googleApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .retryWhen(Retry.backoff(maxRetries - 1, Duration.ofMillis(delayMillis))
                                .filter(throwable -> (throwable instanceof WebClientResponseException &&
                                        ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS))
                                .doBeforeRetry(retrySignal -> log.warn("Retry embedding call for text '{}'. Attempt {} of {}.", text, retrySignal.totalRetries() + 1, maxRetries)));

                @SuppressWarnings("rawtypes")
                Map response = responseMono.block();

                if (response != null && response.containsKey("embedding")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> embeddingMap = (Map<String, Object>) response.get("embedding");
                    if (embeddingMap != null && embeddingMap.containsKey("values")) {
                        @SuppressWarnings("unchecked")
                        List<Double> doubleEmbedding = (List<Double>) embeddingMap.get("values");
                        List<Float> embedding = doubleEmbedding.stream().map(Double::floatValue).toList();

                        if (embedding.size() == embeddingDimension) {
                            return embedding;
                        } else {
                            log.warn("Received embedding with incorrect dimension (expected {}, got {}) for text: '{}'. Retrying if possible.",
                                    embeddingDimension, embedding.size(), text);
                        }
                    }
                }
                log.warn("Gemini returned empty or invalid embedding structure for text: {}", text);
            } catch (Exception e) {
                log.error("Error generating embedding for text: '{}'. Attempt {} of {}. Error: {}", text, retry + 1, maxRetries, e.getMessage());
            }
        }
        log.error("Failed to generate embedding after {} retries for text: '{}'.", maxRetries, text);
        return null;
    }
}
