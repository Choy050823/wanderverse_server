package com.backend.wanderverse_server.listener;

import com.backend.wanderverse_server.config.RabbitMQConfig;
import com.backend.wanderverse_server.model.events.PostCreatedEvent;
import com.backend.wanderverse_server.service.EmbeddingService;
import com.backend.wanderverse_server.service.PostService;
import com.backend.wanderverse_server.service.QdrantService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PostCreationEventListener {
    @Autowired
    private PostService postService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private QdrantService qdrantService;

    @RabbitListener(queues = RabbitMQConfig.NEW_POST_QUEUE_NAME)
    @Transactional
    public void handlePostCreationEvent(PostCreatedEvent postCreatedEvent) {
        log.info("Received PostCreatedEvent for post ID: {}", postCreatedEvent.getPostId());

        try {
            // 1. Fetch the full post data using the postId from the event
            log.info("Fetching full post data for post ID: {}", postCreatedEvent.getPostId());

            postService.getPostById(postCreatedEvent.getPostId())
                    .ifPresentOrElse(
                            post -> {
                                Hibernate.initialize(post.getImageUrls());
                                Hibernate.initialize(post.getCreator());
                                Hibernate.initialize(post.getDestination());

                                String textToEmbed = post.getTitle() + " " + post.getContent();
                                List<Float> embedding = embeddingService.getEmbeddings(
                                        textToEmbed, "RETRIEVAL_DOCUMENT");

                                if (embedding == null || embedding.isEmpty()) {
                                    log.error("Failed to generate embedding for post ID: {}. Skipping Qdrant upload.",
                                            postCreatedEvent.getPostId());
                                    return;
                                }
                                log.info("Successfully generated embedding for post ID: {}", postCreatedEvent.getPostId());

                                // 3. Upload the post embedding to Qdrant
                                log.info("Uploading embedding for post ID: {} to Qdrant.", postCreatedEvent.getPostId());
                                qdrantService.upsertPosts(List.of(post), List.of(embedding));
                                log.info("Post embedding for ID: {} uploaded to Qdrant successfully.", postCreatedEvent.getPostId());
                            },
                            () -> log.error("Could not find post with ID {} for embedding. Skipping.", postCreatedEvent.getPostId())
                    );
        } catch (Exception e) {
            log.error("Error processing PostCreatedEvent for post ID {}: {}", postCreatedEvent.getPostId(), e.getMessage(), e);
        }
    }
}
