package com.example.demo.controllers.MongoDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.requets.updateArticle;
import com.example.demo.models.MongoDB.Articles;
import com.example.demo.services.MongoDB.Articles_service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/Articles")
@Tag(name = "Articles", description = "QUERIES AND AGGREGATION FOR ARTICLES")
public class Articles_controller {

    private final Articles_service articlesService;
    public Articles_controller(Articles_service articlesService) {
        this.articlesService = articlesService;
    }

    // READ: Get a specific article by ID
    @GetMapping("/{id}")
    @Operation(summary = "READ: Get an article by ID")
    public Articles getArticle(@PathVariable String id) {
        return articlesService.getArticle(id);
    }

    // READ: Get all articles with pagination
    @GetMapping("/admin")
    @Operation(summary = "READ: Get all articles with pagination")
    public Page<Articles> getAllArticles(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return articlesService.getAllArticles(pageable);
    }

    // CREATE: Create a new article
    @PostMapping("/admin")
    @Operation(summary = "CREATE: Create a new article")
    public Articles createArticle(@RequestBody Articles article) {
        return articlesService.createArticle(article);
    }

    // UPDATE: Update an existing article by ID
    @PutMapping("/admin/{id}")
    @Operation(summary = "UPDATE: Update an existing article")
    public Articles updateArticle(@PathVariable String id, @RequestBody updateArticle details) {
        return articlesService.updateArticle(id, details);
    }

    // DELETE: Delete an article by ID
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "DELETE: Delete an article by ID")
    public ResponseEntity<Void> deleteArticle(@PathVariable String id) {
        articlesService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
