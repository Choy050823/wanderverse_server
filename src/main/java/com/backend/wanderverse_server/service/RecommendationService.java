package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.post.PostDTO;
import com.backend.wanderverse_server.model.entity.post.PostEntity;
import com.backend.wanderverse_server.model.entity.post.PostType;

import java.util.List;

public interface RecommendationService {

    void ingestPostData();

    List<PostDTO> getRecommendedPostsForUser(Long userId, PostType postType);

    List<PostDTO> getRecommendedPostsByQuery(String query, PostType postType);
}
