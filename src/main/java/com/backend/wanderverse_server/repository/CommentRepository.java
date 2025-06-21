package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostId(Long postId);
}
