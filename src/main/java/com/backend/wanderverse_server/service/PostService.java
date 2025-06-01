package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.CreatePostRequestDTO;
import com.backend.wanderverse_server.model.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Page<PostEntity> findAll(Pageable pageable);

    PostEntity createPost(CreatePostRequestDTO post);

    PostEntity fullUpdatePost(PostEntity post);

    boolean isExists(long id);
}
