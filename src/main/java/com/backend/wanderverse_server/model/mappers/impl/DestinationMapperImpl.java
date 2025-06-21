package com.backend.wanderverse_server.model.mappers.impl;

import com.backend.wanderverse_server.model.dto.DestinationDTO;
import com.backend.wanderverse_server.model.entity.DestinationEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DestinationMapperImpl implements Mapper<DestinationEntity, DestinationDTO> {
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public DestinationDTO mapTo(DestinationEntity destination) {
        return modelMapper.map(destination, DestinationDTO.class);
    }

    @Override
    public DestinationEntity mapFrom(DestinationDTO destinationDTO) {
        return modelMapper.map(destinationDTO, DestinationEntity.class);
    }
}
