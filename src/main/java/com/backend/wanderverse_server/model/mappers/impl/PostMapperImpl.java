package com.backend.wanderverse_server.model.mappers.impl;

import com.backend.wanderverse_server.model.dto.PostDTO;
import com.backend.wanderverse_server.model.entity.PostEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostMapperImpl implements Mapper<PostEntity, PostDTO> {
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PostDTO mapTo(PostEntity postEntity) {
        return modelMapper.map(postEntity, PostDTO.class);
    }

    @Override
    public PostEntity mapFrom(PostDTO postDTO) {
        return modelMapper.map(postDTO, PostEntity.class);
    }
}
