package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.CreatePostRequestDTO;
import com.backend.wanderverse_server.model.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Page<PostEntity> findAll(Pageable pageable);

    Page<PostEntity> findAllSharingPosts(Pageable pageable);

    Page<PostEntity> findSharingPostsByDestination(String destination, Pageable pageable);

    Page<PostEntity> findAllDiscussionPosts(Pageable pageable);

    Page<PostEntity> findDiscussionPostsByDestination(String destination, Pageable pageable);

    PostEntity createPost(CreatePostRequestDTO post);

    PostEntity fullUpdatePost(PostEntity post);

    boolean isExists(long id);
}
