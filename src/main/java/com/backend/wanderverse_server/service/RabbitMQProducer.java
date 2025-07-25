package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.events.PostCreatedEvent;
import com.backend.wanderverse_server.model.events.PostLikedEvent;

public interface RabbitMQProducer {
    void sendPostCreatedEvent(PostCreatedEvent postCreatedEvent);

    void sendPostLikedEvent(PostLikedEvent postLikedEvent);
}
