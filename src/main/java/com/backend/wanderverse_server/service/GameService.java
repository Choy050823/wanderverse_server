package com.backend.wanderverse_server.service;

public interface GameService {
    Integer getGamePoints(Long userId);

    Integer addGamePoints(Long userId, Integer gamePoint);
}
