package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.post.UserDTO;
import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private Mapper<UserEntity, UserDTO> userMapper;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserEntity userEntity = userMapper.mapFrom(userDTO);
        UserEntity savedUser = userService.save(userEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.mapTo(savedUser));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.findOne(id)
                    .map(userMapper::mapTo)
                    .map(user -> ResponseEntity.ok().body(user))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}
