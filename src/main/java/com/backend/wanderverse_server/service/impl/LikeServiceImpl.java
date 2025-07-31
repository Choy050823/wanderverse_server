package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.post.LikeEntity;
import com.backend.wanderverse_server.model.entity.post.PostEntity;
import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.repository.LikeRepository;
import com.backend.wanderverse_server.repository.PostRepository;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.LikeService;
import com.backend.wanderverse_server.util.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class LikeServiceImpl implements LikeService {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional // Ensure transactional for consistency
    public void addLike(Long postId, Long userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if user has already liked the post to prevent duplicates
        if (likeRepository.findLikeWithPostAndUser(postId, userId) != null) {
            log.warn("User {} has already liked post {}", userId, postId);
            return;
        }

        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
        likeRepository.save(LikeEntity.builder().post(post).user(user).build());
        log.info("User {} liked post {}", userId, postId);
    }

    @Override
    public boolean hasUserLikedPost(Long postId, Long userId) {
        return likeRepository.findLikeWithPostAndUser(postId, userId) != null;
    }

    @Override
    @Transactional // Ensure transactional for consistency
    public void removeLike(Long postId, Long userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        LikeEntity likeToDelete = likeRepository.findLikeWithPostAndUser(postId, userId);

        if (likeToDelete == null) {
            log.warn("User {} has not liked post {}. Cannot remove non-existent like.", userId, postId);
            return;
        }

        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(post);
        likeRepository.delete(likeToDelete);
        log.info("User {} unliked post {}", userId, postId);
    }

    @Override
    public List<Long> getUserLikedPostByUserId(Long userId) {
        // It's good practice for repository methods returning lists to return an empty list, not null
        List<LikeEntity> likes = likeRepository.findLikedPostWithUserId(userId);

        if (likes.isEmpty()) { // Check if the list is empty instead of null
            log.info("User {} has not liked any posts yet.", userId); // Changed warn to info
            return List.of();
        }

        return likes.stream().map(like -> like.getPost().getId()).toList();
    }
}

