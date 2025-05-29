package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long>,
        PagingAndSortingRepository<PostEntity, Long> {
}
