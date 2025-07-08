package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.entity.PostEntity;
import com.backend.wanderverse_server.model.entity.PostType;

import java.util.List;

public interface RecommendationService {

    void ingestPostData();

    List<PostEntity> getRecommendedPostsForUser(Long userId, PostType postType);

    List<PostEntity> getRecommendedPostsByQuery(String query, PostType postType);
}
