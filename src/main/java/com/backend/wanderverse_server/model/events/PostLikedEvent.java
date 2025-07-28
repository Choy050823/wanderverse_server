package com.backend.wanderverse_server.model.events;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostLikedEvent {
    private Long likeId;
    private Long userId;
    private LocalDateTime createdAt;
}
