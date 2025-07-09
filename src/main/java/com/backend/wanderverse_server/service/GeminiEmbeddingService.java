package com.backend.wanderverse_server.service;

import java.util.List;

public interface GeminiEmbeddingService {
    List<Float> getEmbeddings(String text, String taskType);
}
