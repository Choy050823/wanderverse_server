package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.entity.auth.UserEntity;

import java.util.Optional;

public interface UserService {
    UserEntity save(UserEntity userEntity);

    Optional<UserEntity> findOne(long id);

    boolean isExists(long id);
}
