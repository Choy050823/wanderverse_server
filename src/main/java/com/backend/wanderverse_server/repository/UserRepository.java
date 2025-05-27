package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findUserByUsername(String username);

    Optional<UserEntity> findUserByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
