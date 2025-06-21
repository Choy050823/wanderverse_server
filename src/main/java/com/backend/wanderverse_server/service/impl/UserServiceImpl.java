package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.UserEntity;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    @Override
    public Optional<UserEntity> findOne(long id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean isExists(long id) {
        return userRepository.existsById(id);
    }
}
