package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.entity.post.PostEntity;
import com.backend.wanderverse_server.repository.PostRepository;
import com.backend.wanderverse_server.service.QdrantService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static io.qdrant.client.VectorFactory.vector;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.QueryFactory.recommend;
import static io.qdrant.client.VectorInputFactory.vectorInput;


import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Service
@Slf4j
public class QdrantServiceImpl implements QdrantService {

    @Value("${qdrant.collection.name}")
    private String collectionName;

    @Value("${qdrant.embedding.dimension}")
    private int embeddingDimension;

    @Autowired
    private QdrantClient qdrantClient;

    @Autowired
    private PostRepository postRepository;

    @Override
    public void createCollection() {
        try {
            boolean isCollectionExists = this.qdrantClient
                    .collectionExistsAsync(this.collectionName)
                    .get();

            if (!isCollectionExists) {
                this.qdrantClient.createCollectionAsync(
                        collectionName,
                        Collections.VectorParams.newBuilder()
                                .setDistance(Collections.Distance.Cosine)
                                .setSize(this.embeddingDimension)
                                .build()
                ).get();
            } else {
                log.info("Collection {} already exists!", this.collectionName);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error creating/checking Qdrant collection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ensure Qdrant collection exists.", e);
        }
    }

    @Override
    public void upsertPosts(List<PostEntity> posts, List<List<Float>> embeddings) throws IllegalArgumentException{
        if (posts.size() != embeddings.size()) {
            log.error("Post Size is not equals Embedding list size. Post size: {}, Embedding Size: {}", posts.size(), embeddings.size());
            throw new IllegalArgumentException("post list size not equals embedding list size");
        }

        try {
            this.qdrantClient.upsertAsync(
                    this.collectionName,
                    Stream.iterate(0, i -> i + 1)
                            .limit(posts.size())
                            .map(i -> {
                                PostEntity post = posts.get(i);
                                List<Float> postEmbedding = embeddings.get(i);

                                String uuid = UUID.nameUUIDFromBytes(
                                        (Long.valueOf(post.getId()).toString()).getBytes()
                                ).toString();

                                return Points.PointStruct.newBuilder()
                                        .setId(Points.PointId
                                                .newBuilder()
                                                .setNum(post.getId())
                                                .build())
                                        .setVectors(Points.Vectors.newBuilder().setVector(vector(postEmbedding)).build())
                                        .build();
                            }).toList()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error upserting post embeddings to Qdrant: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upsert post embeddings.", e);
        }
    }

    @Override
    public List<Float> getEmbeddingsByPostId(Long postId) {
        try {
            // Retrieve embeddings without post details
            List<Points.RetrievedPoint> points = this.qdrantClient.retrieveAsync(
                    collectionName,
                    List.of(Points.PointId
                            .newBuilder()
                            .setUuid(postId.toString())
                            .build()),
                    false,
                    true,
                    null
            ).get();

            if (!points.isEmpty()) {
                if (points.getFirst().getVectors().hasVector()) {
                    return new ArrayList<>(points.getFirst().getVectors().getVector().getDataList());
                } else {
                    log.warn("Point {} found but does not contain a dense vector.", postId);
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error retrieving embedding for post ID {}: {}", postId, e.toString());
            throw new RuntimeException(e);
        }
        return null;
    }

    // provide similar posts that satisfies user search
    // the gemini generates embedding based on user query, and use the query vector/embedding
    // to search in the Qdrant DB
    @Override
    public List<PostEntity> searchSimilarPosts(List<Float> queryVector, Integer limit, List<Long> excludedPostIds) {
        try {
            Points.Filter filter = null;
            if (excludedPostIds != null && !excludedPostIds.isEmpty()) {
                List<Points.PointId> pointIdList = excludedPostIds.stream()
                        .map(postId -> Points.PointId.newBuilder()
                                .setUuid(postId.toString())
                                .build())
                        .toList();

                filter = Points.Filter.newBuilder()
                        .addMustNot(
                                Points.Condition.newBuilder()
                                        .setHasId(Points.HasIdCondition.newBuilder()
                                                        .addAllHasId(pointIdList)
                                                        .build())
                                        .build())
                        .build();


            }

            log.info("Performing Qdrant nearest search query with limit {} and {} exclusions.",
                    limit, excludedPostIds != null ? excludedPostIds.size() : 0);

            Points.QueryPoints.Builder searchBuilder =
                    Points.QueryPoints.newBuilder()
                            .setCollectionName(this.collectionName)
                            .setQuery(nearest(queryVector));

            if (filter != null) {
                searchBuilder.setFilter(filter);
            }

            List<Points.ScoredPoint> searchResults = this.qdrantClient.queryAsync(
                    searchBuilder.setLimit(limit).build()
            ).get();

            return searchResults.stream()
//                    .parallel()
                    .map(this::mapScoredPointsToPost)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error searching similar movies in Qdrant: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search similar movies.", e);
        }
    }

    // recommend posts that is based on past interacted post
    // excluded already interacted posts
    @Override
    public List<PostEntity> recommendPosts(List<Long> interactedPostsIds, Integer limit, List<Long> excludedPostIds) {
        if (interactedPostsIds == null || interactedPostsIds.isEmpty()) {
            log.warn("No positive post IDs provided for recommendation. Returning empty list.");
            return List.of();
        }

        try {
            Points.RecommendInput.Builder recommendInput = Points.RecommendInput.newBuilder();

            for (Long postId : interactedPostsIds) {
//                recommendInput.addPositive(vectorInput(UUID.fromString(postId.toString())));
                recommendInput.addPositive(vectorInput(Points.PointId.newBuilder().setNum(postId).build()));
            }

            if (recommendInput.getPositiveList().isEmpty()) {
                log.warn("No valid positive IDs to use for recommendation after filtering invalid UUIDs");
                return List.of();
            }

            Points.Filter filter = null;
            if (excludedPostIds != null && !excludedPostIds.isEmpty()) {
                List<Points.PointId> excludePointIds = excludedPostIds.stream()
                        .map(id -> {
                            try {
                                return Points.PointId.newBuilder().setNum(id).build();
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid UUID format, {}", e.toString());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

                if (!excludedPostIds.isEmpty()) {
                    filter = Points.Filter.newBuilder()
                            .addMustNot(
                                    Points.Condition.newBuilder()
                                            .setHasId(Points.HasIdCondition
                                                    .newBuilder()
                                                    .addAllHasId(excludePointIds)
                                                    .build())
                                            .build()
                            ).build();
                }
            }

            log.info("Performing Qdrant recommend query for {} positive IDs with limit {} and {} exclusions.",
                    interactedPostsIds.size(), limit, excludedPostIds != null ? excludedPostIds.size() : 0);

            Points.QueryPoints.Builder recommendBuilder = Points.QueryPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .setQuery(recommend(recommendInput.build()));

            if (filter != null) {
                recommendBuilder.setFilter(filter);
            }

            List<Points.ScoredPoint> recommendResult = this.qdrantClient.queryAsync(
                    recommendBuilder.setLimit(limit).build()
            ).get();

            return recommendResult.stream()
//                    .parallel()
                    .map(this::mapScoredPointsToPost)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error recommending posts from Qdrant: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to recommend posts.", e);
        }
    }

    private PostEntity mapScoredPointsToPost(Points.ScoredPoint point) {
        return postRepository
                .findById(point.getId().getNum())
                .orElse(null);
    }
}
