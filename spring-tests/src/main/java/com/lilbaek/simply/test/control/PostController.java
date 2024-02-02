package com.lilbaek.simply.test.control;

import com.lilbaek.simply.test.model.Post;
import com.lilbaek.simply.test.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(final PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}")
    Optional<Post> findById(@PathVariable final String id) {
        return postService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void create(@RequestBody final Post post) {
        postService.create(post);
    }

    @PutMapping("/{id}")
    void update(@RequestBody final Post post, @PathVariable final String id) {
        postService.update(post, id);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable final String id) {
        postService.delete(id);
    }
}
