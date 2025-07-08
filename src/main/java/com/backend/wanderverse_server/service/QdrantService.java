package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.entity.PostEntity;

import java.util.List;

public interface QdrantService {
    void createCollection();

    void upsertPosts(List<PostEntity> posts, List<List<Float>> embeddings);

    List<Float> getEmbeddingsByPostId(Long postId);

    List<PostEntity> searchSimilarPosts(List<Float> queryVector, Integer limit, List<Long> excludedPostIds);

    List<PostEntity> recommendPosts(List<Long> interactedPostsIds, Integer limit, List<Long> excludedPostIds);
}
