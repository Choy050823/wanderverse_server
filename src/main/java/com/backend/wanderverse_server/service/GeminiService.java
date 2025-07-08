package com.backend.wanderverse_server.service;

import java.util.List;

public interface GeminiService {
    List<Float> getEmbeddings(String text, String taskType);
}
