package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    @Query("SELECT a FROM LikeEntity a WHERE a.post.id = :postId AND a.user.id = :userId")
    LikeEntity findLikeWithPostAndUser(Long postId, Long userId);
}
