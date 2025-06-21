package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.entity.DestinationEntity;

import java.util.List;
import java.util.Optional;

public interface DestinationService {
    List<DestinationEntity> findAll();

    Optional<DestinationEntity> findOne(Long id);

    DestinationEntity save(DestinationEntity destination);
}
