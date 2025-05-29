package com.backend.wanderverse_server.repository;

import com.backend.wanderverse_server.model.entity.DestinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<DestinationEntity, Long> {
}
