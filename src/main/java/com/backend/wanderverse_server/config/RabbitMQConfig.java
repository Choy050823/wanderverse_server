package com.backend.wanderverse_server.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // New post creation messaging queue
    public static final String POST_EXCHANGE_NAME = "post-exchange";
    public static final String NEW_POST_ROUTING_KEY = "new-post-key";
    public static final String NEW_POST_QUEUE_NAME = "new-post-queue";

    // New like post-messaging queue
    public static final String POST_LIKE_EXCHANGE_NAME = "post-like-exchange";
    public static final String POST_LIKE_ROUTING_KEY = "like-post-key";
    public static final String POST_LIKE_QUEUE_NAME = "like-post-queue";

    // POST-CREATION MESSAGE CONFIG
    @Bean
    public DirectExchange newPostCreationExchange() {
        return new DirectExchange(POST_EXCHANGE_NAME);
    }

    @Bean
    public Queue postMessagingQueue() {
        // Make the messaging queue can survive failure
        return new Queue(NEW_POST_QUEUE_NAME, true);
    }

    @Bean
    public Binding postCreationBinding(Queue postMessagingQueue, DirectExchange newPostCreationExchange) {
        return BindingBuilder.bind(postMessagingQueue).to(newPostCreationExchange).with(NEW_POST_ROUTING_KEY);
    }

    // POST LIKE MESSAGE CONFIG
    @Bean
    public DirectExchange newPostLikeExchange() {
        return new DirectExchange(POST_LIKE_EXCHANGE_NAME);
    }

    @Bean
    public Queue postLikeMessagingQueue() {
        // Make the messaging queue can survive failure
        return new Queue(POST_LIKE_QUEUE_NAME, true);
    }

    @Bean
    public Binding postLikeBinding(Queue postLikeMessagingQueue, DirectExchange newPostLikeExchange) {
        return BindingBuilder.bind(postLikeMessagingQueue).to(newPostLikeExchange).with(POST_LIKE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

//    @Bean
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
