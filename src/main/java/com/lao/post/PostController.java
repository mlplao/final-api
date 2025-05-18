package com.lao.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postRepository.findAll());
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createPostsBulk(@RequestBody List<Post> posts) {
        // Validate the request body
        if (posts == null || posts.isEmpty()) {
            return ResponseEntity.badRequest().body("The request body must contain a list of posts.");
        }

        // Set timestamps for each post
        posts.forEach(post -> {
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(post.getCreatedAt());
        });

        // Save posts to the database
        try {
            List<Post> savedPosts = postRepository.saveAll(posts);
            return new ResponseEntity<>(savedPosts, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving posts.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(post.getCreatedAt()); // Ensure updatedAt is set to createdAt
        Post savedPost = postRepository.save(post);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postRepository.findById(id)
                .map(existingPost -> {
                    existingPost.setContent(post.getContent());
                    existingPost.setImageUrl(post.getImageUrl());
                    existingPost.setAuthor(post.getAuthor());
                    existingPost.setUpdatedAt(LocalDateTime.now()); // Update updatedAt timestamp
                    Post updatedPost = postRepository.save(existingPost);
                    return ResponseEntity.ok(updatedPost);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}