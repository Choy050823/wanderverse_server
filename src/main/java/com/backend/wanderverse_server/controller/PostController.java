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

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private Mapper<PostEntity, PostDTO> postMapper;

    @GetMapping(path = "/listPosts")
    public Page<PostDTO> listPosts(Pageable pageable) {
        return postService.findAll(pageable).map(postMapper::mapTo);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequestDTO post) {
//        boolean isExists = postService.isExists(Long.parseLong(post.getId()));
        PostDTO savedPost = postMapper.mapTo(postService.createPost(post));

        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }
}
