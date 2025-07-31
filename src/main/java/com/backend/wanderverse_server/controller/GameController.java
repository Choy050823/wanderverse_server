package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @GetMapping(path = "/points")
    public ResponseEntity<Integer> getGamePoints(@RequestParam Long userId) {
        Integer gamePoint = gameService.getGamePoints(userId);
        return ResponseEntity.ok().body(gamePoint);
    }

    @PostMapping(path = "/points")
    public ResponseEntity<Integer> addGamePoints(@RequestParam Long userId, @RequestParam Integer newGamePoint) {
        Integer gamePoint = gameService.addGamePoints(userId, newGamePoint);
        return ResponseEntity.status(HttpStatus.CREATED).body(gamePoint);
    }

    @GetMapping(path = "/badges")
    public ResponseEntity<List<String>> getUserBadges(@RequestParam Long userId) {
        return ResponseEntity.ok().body(gameService.getUserAchievementBadgesImageUrl(userId));
    }

    @PostMapping(path = "/badges")
    public ResponseEntity<String> achievementUnlocked(@RequestParam Long userId, @RequestParam String achievementName) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.achievementUnlocked(userId, achievementName));
    }
}
