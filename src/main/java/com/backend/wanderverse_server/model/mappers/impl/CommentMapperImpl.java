package com.backend.wanderverse_server.model.mappers.impl;

import com.backend.wanderverse_server.model.dto.CommentDTO;
import com.backend.wanderverse_server.model.entity.CommentEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMapperImpl implements Mapper<CommentEntity, CommentDTO> {
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CommentDTO mapTo(CommentEntity commentEntity) {
        return modelMapper.map(commentEntity, CommentDTO.class);
    }

    @Override
    public CommentEntity mapFrom(CommentDTO commentDTO) {
        return modelMapper.map(commentDTO, CommentEntity.class);
    }
}
