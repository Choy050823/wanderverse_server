package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.DestinationDTO;
import com.backend.wanderverse_server.model.entity.DestinationEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destination")
public class DestinationController {
    @Autowired
    private DestinationService destinationService;

    @Autowired
    private Mapper<DestinationEntity, DestinationDTO> destinationMapper;

    @GetMapping
    public ResponseEntity<List<DestinationDTO>> getDestinationList() {
        return ResponseEntity.ok().body(
                destinationService.findAll()
                                    .stream()
                                    .map(destinationMapper::mapTo)
                                    .toList()
        );
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<DestinationDTO> getDestination(@PathVariable Long id) {
        return destinationService.findOne(id)
                .map(destinationMapper::mapTo)
                .map(destination -> ResponseEntity.ok().body(destination))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping
    public ResponseEntity<DestinationDTO> createDestination(@RequestBody DestinationDTO destinationDTO) {
        DestinationEntity destinationEntity = destinationMapper.mapFrom(destinationDTO);
        DestinationEntity savedDestination = destinationService.save(destinationEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(destinationMapper.mapTo(savedDestination));
    }
}
