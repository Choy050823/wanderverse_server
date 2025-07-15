package com.backend.wanderverse_server.config;

import com.google.genai.Client;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AIConfig {
    // AI Agent Credentials
    @Value("${google.cloud.project.id}")
    private String googleCloudProjectId;

    @Value("${google.cloud.project.location}")
    private String googleCloudProjectLocation;

    @Value("${google.genai.use.vertexai}")
    private boolean useVertexAI;

//    @Value("${gemini.api.key}")
//    private String aiApiKey;

    // Embedding Model credentials
    @Value("${google.embedding.baseurl}")
    private String googleEmbeddingBaseUrl;

    // Qdrant Vector Database Credentials
    @Value("${qdrant.host}")
    private String qdrantHost;

    @Value("${qdrant.api.key}")
    private String qdrantApiKey;

    @Bean
    public Client aiAgent() {
        return Client.builder()
                .project(googleCloudProjectId)
                .location(googleCloudProjectLocation)
                .vertexAI(useVertexAI)
//                .apiKey(aiApiKey)
                .build();
    }

    @Bean
    public WebClient embeddingWebClient() {
        return WebClient.builder()
                .baseUrl(googleEmbeddingBaseUrl)
                .build();
    }

    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(
                    qdrantHost,
                    6334,
                    true
            ).withApiKey(this.qdrantApiKey).build()
        );
    }
}
