package com.backend.wanderverse_server.model.mappers.impl;

import com.backend.wanderverse_server.model.dto.UserDTO;
import com.backend.wanderverse_server.model.entity.UserEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements Mapper<UserEntity, UserDTO> {

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDTO mapTo(UserEntity userEntity) {
        return modelMapper.map(userEntity, UserDTO.class);
    }

    @Override
    public UserEntity mapFrom(UserDTO userDTO) {
        return modelMapper.map(userDTO, UserEntity.class);
    }
}
