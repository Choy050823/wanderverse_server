package com.backend.wanderverse_server.service;

import java.util.List;

public interface EmbeddingService {
    List<Float> getEmbeddings(String text, String taskType);
}
