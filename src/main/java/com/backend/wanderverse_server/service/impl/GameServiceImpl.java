package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.UserEntity;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.GameService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {
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

        return getGamePoints(userId);
    }
}
