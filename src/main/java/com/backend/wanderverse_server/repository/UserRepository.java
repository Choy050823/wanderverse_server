package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findUserByUsername(String username);

    UserEntity findUserByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
