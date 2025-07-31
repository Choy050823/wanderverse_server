package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.post.CommentDTO;
import com.backend.wanderverse_server.model.dto.post.CommentRequestDTO;
import com.backend.wanderverse_server.model.entity.post.CommentEntity;
import com.backend.wanderverse_server.model.entity.post.PostEntity;
import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.repository.CommentRepository;
import com.backend.wanderverse_server.repository.PostRepository;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.service.CommentService;
import com.backend.wanderverse_server.util.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public CommentEntity createComment(CommentRequestDTO comment) {
        long postId;
        long userId;
        Long parentCommentId = null;

        try {
            // Parse and validate post id
            postId = Long.parseLong(comment.getPostId());
            userId = Long.parseLong(comment.getUserId());
            if (comment.getParentCommentId() != null && !comment.getParentCommentId().isEmpty()) {
                parentCommentId = Long.parseLong(comment.getParentCommentId());
            }
        } catch (NumberFormatException e) {
            // Throw specific exception for invalid ID format
            throw new RuntimeException("One or more provided IDs are in an invalid format.", e);
        }

        // Parse and validate post id
        PostEntity post = postRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with ID: " + comment.getPostId() + " not found"));

        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID: " + comment.getUserId() + " not found"));

        // Handle parent comment
        CommentEntity parentComment = null;
        if (parentCommentId != null) {
            Long finalParentCommentId = parentCommentId;
            parentComment = commentRepository
                    .findById(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment with ID " + finalParentCommentId + " not found."));
        }

        CommentEntity commentEntity = CommentEntity.builder()
                .content(comment.getContent())
                .post(post)
                .user(user)
                .parentComment(parentComment)
                .build();

        CommentEntity savedCommentEntity = commentRepository.save(commentEntity);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        return savedCommentEntity;
    }

    @Override
    public List<CommentEntity> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    // Special case for comments because need to provide all its replies together
    @Override
    public List<CommentDTO> getCommentTreeByPost(Long postId) {
        // Fetch all comments from post
        List<CommentEntity> allComments = commentRepository.findByPostId(postId);

        // Group comments by parent
        Map<Long, List<CommentEntity>> commentsByParentId = new HashMap<>();
        List<CommentEntity> rootComments = new ArrayList<>();

        for (CommentEntity comment : allComments) {
            if (comment.getParentComment() == null) {
                rootComments.add(comment);
            } else {
                Long parentId = comment.getParentComment().getId();
                commentsByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
            }
        }

        // Build comment tree structure
        return buildCommentTree(rootComments, commentsByParentId);
    }

    private List<CommentDTO> buildCommentTree(
            List<CommentEntity> comments, Map<Long, List<CommentEntity>> commentsByParentCommentId) {
        return comments.stream().map(comment -> CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .post(comment.getPost())
                .user(comment.getUser())
                .createdAt(comment.getCreatedAt())
                .replies(buildCommentTree(
                        commentsByParentCommentId.getOrDefault(
                                comment.getId(),
                                Collections.emptyList()),
                        commentsByParentCommentId
                )).build()).collect(Collectors.toList());
    }
}
