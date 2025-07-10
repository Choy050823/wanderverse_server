package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @GetMapping(path = "/points")
    public ResponseEntity<Integer> getGamePoints(@RequestParam Long userId) {
        Integer gamePoint = gameService.getGamePoints(userId);
        return gamePoint != null ? ResponseEntity.ok().body(gamePoint)
                                 : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping(path = "/points")
    public ResponseEntity<Integer> addGamePoints(@RequestParam Long userId, @RequestParam Integer newGamePoint) {
        Integer gamePoint = gameService.addGamePoints(userId, newGamePoint);
        return gamePoint != null ? ResponseEntity.status(HttpStatus.CREATED).body(gamePoint)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
