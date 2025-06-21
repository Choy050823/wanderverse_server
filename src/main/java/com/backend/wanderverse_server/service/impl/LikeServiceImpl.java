package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.LikeEntity;
import com.backend.wanderverse_server.model.entity.PostEntity;
import com.backend.wanderverse_server.model.entity.UserEntity;
import com.backend.wanderverse_server.repository.LikeRepository;
import com.backend.wanderverse_server.repository.PostRepository;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void addLike(Long postId, Long userId) {
        PostEntity post = postRepository.findById(postId)
                                        .orElseThrow(() -> new RuntimeException("Cannot find post"));

        UserEntity user = userRepository.findById(userId)
                                        .orElseThrow(() -> new RuntimeException("User not found"));

        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
        likeRepository.save(LikeEntity.builder().post(post).user(user).build());
    }

    @Override
    public boolean hasUserLikedPost(Long postId, Long userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Cannot find post"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return likeRepository.findLikeWithPostAndUser(postId, userId) != null;
    }

    @Override
    public void removeLike(Long postId, Long userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Cannot find post"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!hasUserLikedPost(postId, userId)) {
            return;
        }

        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(post);
        likeRepository.delete(likeRepository.findLikeWithPostAndUser(postId, userId));
    }
}
