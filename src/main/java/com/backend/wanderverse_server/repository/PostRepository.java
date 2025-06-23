package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long>,
        PagingAndSortingRepository<PostEntity, Long> {

    @Query("SELECT p FROM PostEntity p WHERE p.postType IN :postTypes ORDER BY p.createdAt DESC")
    Page<PostEntity> findAllByPostTypes(@Param("postTypes") List<String> postTypes, Pageable pageable);

    @Query("SELECT p FROM PostEntity p WHERE p.postType IN :postTypes AND p.destination.id = :destinationId ORDER BY p.createdAt DESC")
    Page<PostEntity> findByPostTypesAndDestination(
            @Param("postTypes") List<String> postTypes,
            Long destinationId,
            Pageable pageable);
}
