package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.post.PostDTO;
import com.backend.wanderverse_server.model.entity.post.PostEntity;
import com.backend.wanderverse_server.model.entity.post.PostType;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.repository.PostRepository;
import com.backend.wanderverse_server.service.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private QdrantService qdrantService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private Mapper<PostEntity, PostDTO> postMapper;

    @Value("${recommendation.batch.size.embedding}")
    private int embeddingBatchSize;

    @Value("${recommendation.batch.size.qdrant}")
    private int qdrantBatchSize;

    @Value("${recommendation.limit.per.user}")
    private int recommendPostLimit;

    @Override
    @Scheduled(fixedRateString = "${ingestion.schedule.rate}")
    public void ingestPostData() {
        log.info("Starting post data ingestion from PostgreSQL...");
        qdrantService.createCollection();

        List<PostEntity> allPosts = postRepository.findAll();
        log.info("Fetched {} posts from PostgreSQL database for ingestion.", allPosts.size());

        List<PostEntity> postsBatch = new ArrayList<>();
        List<List<Float>> embeddingsBatch = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        allPosts.forEach(
                post -> {
                    String textToEmbed = post.getTitle() + " " + post.getContent();
                    List<Float> embedding = embeddingService.getEmbeddings(textToEmbed, "RETRIEVAL_DOCUMENT");

                    if (embedding != null) {
                        postsBatch.add(post);
                        embeddingsBatch.add(embedding);
                        processedCount.incrementAndGet();
                    } else {
                        log.warn("Skipped post '{}' (ID: {}) due to embedding failure.", post.getTitle(), post.getId());
                        skippedCount.incrementAndGet();
                    }

                    // If exceed batchSize, upsert first then clear data
                    if (postsBatch.size() >= qdrantBatchSize) {
                        qdrantService.upsertPosts(postsBatch, embeddingsBatch);
                        postsBatch.clear();
                        embeddingsBatch.clear();
                    }
                }
        );

        // upsert remaining ones
        if (!postsBatch.isEmpty()) {
            qdrantService.upsertPosts(postsBatch, embeddingsBatch);
        }

        log.info("Posts data ingestion complete. Processed: {} | Skipped: {}", processedCount.get(), skippedCount.get());
    }

    @Override
    @Transactional
    @Cacheable(value = "recommended_posts", key = "'post:rec:user:' + #userId + ':type:' + (#postType != null ? #postType.name() : 'ALL')")
    public List<PostDTO> getRecommendedPostsForUser(Long userId, PostType postType) {
        log.info("Getting personalized recommendations for user: {}", userId);
        List<Long> interactedPostIdList = likeService.getUserLikedPostByUserId(userId);

        if (interactedPostIdList.isEmpty()) {
            log.warn("No interaction found for user {}, cannot generate personalised recommendation!", userId);
            return List.of();
        }

        List<PostEntity> posts = qdrantService.recommendPosts(
                interactedPostIdList,
                recommendPostLimit,
                interactedPostIdList
        );

        posts.forEach(post -> {
            Hibernate.initialize(post.getImageUrls());
            Hibernate.initialize(post.getCreator());
            Hibernate.initialize(post.getDestination());
        });

        return posts.stream()
                .map(postMapper::mapTo)
                .toList();
    }

    @Override
    @Transactional
    @Cacheable(value = "query_posts", key = "'post:query:' + T(java.util.Base64).getEncoder().encodeToString(#query.getBytes()).substring(0,22) + ':type:' + (#postType != null ? #postType.name() : 'ALL')")
    public List<PostDTO> getRecommendedPostsByQuery(String query, PostType postType) {
        if (query == null || query.isBlank()) {
            log.error("Query cannot be empty!");
            return List.of();
        }

        log.info("Getting generic recommendations for query: '{}'", query);

        List<Float> queryEmbedding = embeddingService.getEmbeddings(query, "RETRIEVAL_QUERY");

        if (queryEmbedding == null) {
            log.error("Failed to generate embedding for query: '{}'", query);
            return List.of();
        }

        log.info("Successfully generated embedding for query with size: {}", queryEmbedding.size());

        List<PostEntity> posts = qdrantService.searchSimilarPosts(
                queryEmbedding,
                recommendPostLimit,
                null
        );

        posts.forEach(post -> {
            Hibernate.initialize(post.getImageUrls());
            Hibernate.initialize(post.getCreator());
            Hibernate.initialize(post.getDestination());
        });

        return posts.stream()
                .map(postMapper::mapTo)
                .toList();
    }
}
