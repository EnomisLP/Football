package com.example.demo.controllers.MongoDB;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.demo.models.MongoDB.Articles;
import com.example.demo.services.MongoDB.Articles_service;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;



@RestController
@RequestMapping("/api/v1/")


public class Articles_controller {

    private final Articles_service articlesService;
    public Articles_controller(Articles_service articlesService) {
        this.articlesService = articlesService;
    }

    /*// READ: Get a specific article by ID
    @GetMapping("article/{id}")
    @Operation(summary = "READ: Get an article by ID", tags={"Article"})
    public Articles getArticle(@PathVariable String id) {
        return articlesService.getArticle(id);
    }*/
    
    // READ: Get a specific article by ID
    @GetMapping("article/{id}")
    @Operation(summary = "READ: Get an article by ID", tags={"Article"})
    public ResponseEntity<Articles> getArticle(@PathVariable String id) {
        Articles result=articlesService.getArticle(id);
        if (result != null) {
            return ResponseEntity.ok(result); // HTTP 200 OK
        } else {
            return ResponseEntity.notFound().build(); // HTTP 404 Not Found

        }

    }

    // READ: Get all articles with pagination
    @GetMapping("admin/articles")
    @Operation(summary = "READ: Get all articles with pagination", tags={"Admin:Article"})
    public Page<Articles> getAllArticles(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return articlesService.getAllArticles(pageable);
    }

    @DeleteMapping("admin/article/delete/{articleId}")
    @Operation(summary = "Delete an article by its id", tags={"Admin:Article"})
    public CompletableFuture<String> deleteArticle(@PathVariable String articleId, Authentication auth) throws JsonProcessingException {
        return articlesService.deleteArticle(auth.getName(), articleId);
    }

}