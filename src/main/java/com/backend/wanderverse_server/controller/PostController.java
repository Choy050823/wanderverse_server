package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.CreatePostRequestDTO;
import com.backend.wanderverse_server.model.dto.PostDTO;
import com.backend.wanderverse_server.model.entity.PostEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostService postService;

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

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequestDTO post) {
        return Optional.of(postMapper.mapTo(postService.createPost(post)))
                .map((postDTO) -> ResponseEntity.status(HttpStatus.CREATED).body(postDTO))
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
    }
}
