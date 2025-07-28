package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.post.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    @Query("SELECT a FROM LikeEntity a WHERE a.post.id = :postId AND a.user.id = :userId")
    LikeEntity findLikeWithPostAndUser(Long postId, Long userId);

    @Query("SELECT a FROM LikeEntity a WHERE a.user.id = :userId")
    List<LikeEntity> findLikedPostWithUserId(Long userId);
}
