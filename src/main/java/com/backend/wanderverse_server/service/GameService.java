package com.backend.wanderverse_server.service;

import java.util.List;

public interface GameService {
    Integer getGamePoints(Long userId);

    Integer addGamePoints(Long userId, Integer gamePoint);

    List<String> getUserAchievementBadgesImageUrl(Long userId);

    String achievementUnlocked(Long userId, String achievementName);
}
