package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.CreatePostRequestDTO;
import com.backend.wanderverse_server.model.dto.PostDTO;
import com.backend.wanderverse_server.model.entity.DestinationEntity;
import com.backend.wanderverse_server.model.entity.PostEntity;
import com.backend.wanderverse_server.model.entity.UserEntity;
import com.backend.wanderverse_server.repository.DestinationRepository;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.backend.wanderverse_server.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Override
    public Page<PostEntity> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Override
    public PostEntity createPost(CreatePostRequestDTO post) {
        try {
            // Validate and parse creatorId
            Long creatorId = Long.parseLong(post.getCreatorId());
            UserEntity creator = userRepository.findById(creatorId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + creatorId));

            // Validate and parse destinationId
            Long destinationId = Long.parseLong(post.getDestinationId());
            DestinationEntity destination = destinationRepository.findById(destinationId)
                    .orElseThrow(() -> new RuntimeException("Destination not found with ID: " + destinationId));

            // Build PostEntity
            PostEntity postEntity = PostEntity.builder()
                    .title(post.getTitle())
                    .content(post.getContent())
                    .imageUrls(post.getImageUrls())
                    .creator(creator) // Explicitly set UserEntity
                    .destination(destination) // Explicitly set DestinationEntity
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .likesCount(0)
                    .commentsCount(0)
                    .build();

            return postRepository.save(postEntity);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid ID format: " + e.getMessage());
        }
    }

    @Override
    public boolean isExists(long id) {
        return postRepository.existsById(id);
    }

    @Override
    public PostEntity fullUpdatePost(PostEntity post) {
        return postRepository.save(post);
    }
}