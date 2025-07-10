package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.post.DestinationEntity;
import com.backend.wanderverse_server.repository.DestinationRepository;
import com.backend.wanderverse_server.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DestinationServiceImpl implements DestinationService {
    @Autowired
    private DestinationRepository destinationRepository;

    @Override
    public List<DestinationEntity> findAll() {
        return destinationRepository.findAll();
    }

    @Override
    public Optional<DestinationEntity> findOne(Long id) {
        return destinationRepository.findById(id);
    }

    @Override
    public DestinationEntity save(DestinationEntity destination) {
        return destinationRepository.save(destination);
    }
}
