package com.backend.wanderverse_server.service;

public interface LikeService {
    void addLike(Long postId, Long userId);

    boolean hasUserLikedPost(Long postId, Long userId);

    void removeLike(Long postId, Long userId);
}
