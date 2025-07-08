package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.CreatePostRequestDTO;
import com.backend.wanderverse_server.model.dto.PostDTO;
import com.backend.wanderverse_server.model.entity.PostEntity;
import com.backend.wanderverse_server.model.entity.PostType;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.service.PostService;
import com.backend.wanderverse_server.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private Mapper<PostEntity, PostDTO> postMapper;

    @GetMapping(path = "/all")
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return postService
                .findAll(pageable)
                .map(postMapper::mapTo);
    }

    @GetMapping(path = "/sharing")
    public Page<PostDTO> getSharingPosts(@RequestParam String destinationId, Pageable pageable) {
        return postService
                .findSharingPostsByDestination(destinationId, pageable)
                .map(postMapper::mapTo);
    }

    @GetMapping(path = "/discussion")
    public Page<PostDTO> getDiscussionPosts(@RequestParam String destinationId, Pageable pageable) {
        return postService
                .findDiscussionPostsByDestination(destinationId, pageable)
                .map(postMapper::mapTo);
    }

    // NOTE: The search and recommend service should implement these later
    // 1. filter according to destination and post type
    // 2. pagination support with endless scrolling

    @GetMapping(path = "/sharing/search")
    public ResponseEntity<List<PostDTO>> searchSharingPosts(@RequestParam String query) {
        return ResponseEntity.ok().body(
                recommendationService
                        .getRecommendedPostsByQuery(query, PostType.post)
                        .stream()
                        .map(postMapper::mapTo)
                        .toList()
        );
    }

    @GetMapping(path = "/discussion/search")
    public ResponseEntity<List<PostDTO>> searchDiscussionPosts(@RequestParam String query) {
        return ResponseEntity.ok().body(
                recommendationService
                        .getRecommendedPostsByQuery(query, PostType.experience)
                        .stream()
                        .map(postMapper::mapTo)
                        .toList()
        );
    }

    @GetMapping(path = "/sharing/recommend")
    public ResponseEntity<List<PostDTO>> recommendSharingPosts(@RequestParam Long userId) {
        return ResponseEntity.ok().body(
                recommendationService
                        .getRecommendedPostsForUser(userId, PostType.post)
                        .stream()
                        .map(postMapper::mapTo)
                        .toList()
        );
    }

    @GetMapping(path = "/discussion/recommend")
    public ResponseEntity<List<PostDTO>> recommendDiscussionPosts(@RequestParam Long userId) {
        return ResponseEntity.ok().body(
                recommendationService
                        .getRecommendedPostsForUser(userId, PostType.experience)
                        .stream()
                        .map(postMapper::mapTo)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequestDTO post) {
        return Optional.of(postMapper.mapTo(postService.createPost(post)))
                .map((postDTO) -> ResponseEntity.status(HttpStatus.CREATED).body(postDTO))
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
    }
}
