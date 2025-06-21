package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
public class LikeController {
    @Autowired
    private LikeService likeService;

    @PostMapping
    public ResponseEntity<Void> addLike(@RequestParam String postId, @RequestParam String userId) {
        likeService.addLike(Long.parseLong(postId), Long.parseLong(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping
    public ResponseEntity<Boolean> hasUserLikedPost(@RequestParam String postId, @RequestParam String userId) {
        return ResponseEntity.ok().body(
                likeService.hasUserLikedPost(Long.parseLong(postId), Long.parseLong(userId))
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> removeLike(@RequestParam String postId, @RequestParam String userId) {
        likeService.removeLike(Long.parseLong(postId), Long.parseLong(userId));
        return ResponseEntity.ok().body(null);
    }
}
