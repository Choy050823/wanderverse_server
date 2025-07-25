package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.config.RabbitMQConfig;
import com.backend.wanderverse_server.model.events.PostCreatedEvent;
import com.backend.wanderverse_server.model.events.PostLikedEvent;
import com.backend.wanderverse_server.service.RabbitMQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQProducerImpl implements RabbitMQProducer {
    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Override
    public void sendPostCreatedEvent(PostCreatedEvent postCreatedEvent) {
        log.info("Publishing PostCreatedEvent for post ID: {}", postCreatedEvent.getPostId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POST_EXCHANGE_NAME,
                RabbitMQConfig.NEW_POST_ROUTING_KEY,
                postCreatedEvent
        );
        log.info("PostCreatedEvent published successfully!");
    }

    @Override
    public void sendPostLikedEvent(PostLikedEvent postLikedEvent) {
        log.info("Publishing PostLikedEvent for like ID: {}", postLikedEvent.getLikeId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POST_LIKE_EXCHANGE_NAME,
                RabbitMQConfig.POST_LIKE_ROUTING_KEY,
                postLikedEvent
        );
        log.info("PostLikedEvent published successfully!");
    }
}
