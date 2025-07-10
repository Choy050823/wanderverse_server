package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.GameService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Override
    public Integer getGamePoints(Long userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getGamePoints)
                .orElse(null);
    }

    @Override
    @Transactional
    public Integer addGamePoints(Long userId, Integer gamePoint) {
        userRepository
                .findById(userId)
                .ifPresent(user -> user.setGamePoints(user.getGamePoints() + gamePoint));

        log.info("New game point for {} : {}", userId, userRepository.findById(userId).get().getGamePoints());

        return getGamePoints(userId);
    }
}
