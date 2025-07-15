package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.GameService;
import com.backend.wanderverse_server.service.StorageService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

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

    @Override
    public List<String> getUserAchievementBadgesImageUrl(Long userId) {
        return userRepository
                .findById(userId)
                .map(UserEntity::getBadgesUrls)
                .orElse(List.of());
    }

    @Override
    @Transactional
    public String achievementUnlocked(Long userId, String achievementName) {
        userRepository
                .findById(userId)
                .ifPresent(user -> {
                    List<String> badges = user.getBadgesUrls();
                    badges.add(storageService.generateFileUrl(achievementName));
                    user.setBadgesUrls(badges);
                });

        return userRepository
                .findById(userId)
                .map(user -> user.getBadgesUrls().getLast())
                .orElseThrow(() -> new RuntimeException("Cannot get new badge"));
    }
}
