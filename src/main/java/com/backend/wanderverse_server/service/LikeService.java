package com.backend.wanderverse_server.service;

import java.util.List;

public interface LikeService {
    void addLike(Long postId, Long userId);

    boolean hasUserLikedPost(Long postId, Long userId);

    void removeLike(Long postId, Long userId);

    List<Long> getUserLikedPostByUserId(Long userId);
}
