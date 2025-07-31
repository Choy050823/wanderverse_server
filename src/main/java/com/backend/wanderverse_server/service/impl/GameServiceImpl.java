package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.GameService;
import com.backend.wanderverse_server.util.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import com.backend.wanderverse_server.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    @Override
    public Integer getGamePoints(Long userId) {
        // Throw ResourceNotFoundException if user is not found, for consistency
        return userRepository.findById(userId)
                .map(UserEntity::getGamePoints)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    @Transactional
    public Integer addGamePoints(Long userId, Integer gamePoint) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId)); // Explicitly throw if user not found

        user.setGamePoints(user.getGamePoints() + gamePoint);
        userRepository.save(user); // Save the updated user
        return user.getGamePoints(); // Return the updated points directly
    }

    @Override
    public List<String> getUserAchievementBadgesImageUrl(Long userId) {
        return userRepository
                .findById(userId)
                .map(UserEntity::getBadgesUrls)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    @Transactional
    public String achievementUnlocked(Long userId, String achievementName) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId)); // Explicitly throw if user not found

        List<String> badges = Optional.ofNullable(user.getBadgesUrls()).orElseGet(ArrayList::new); // Initialize if null
        String newBadgeUrl = storageService.generateFileUrl(achievementName);

        if (!badges.contains(newBadgeUrl)) { // Prevent duplicate badges
            badges.add(newBadgeUrl);
            user.setBadgesUrls(badges);
            userRepository.save(user); // Save the updated user
            log.info("User {} unlocked new achievement: {}", userId, achievementName);
        } else {
            log.warn("User {} already has achievement: {}", userId, achievementName);
        }
        return newBadgeUrl;
    }
}
