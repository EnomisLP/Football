package com.example.demo.controllers.Neo4j;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.ArticlesNodeDTO;
import com.example.demo.services.Neo4j.Articles_node_service;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/v1/")


public class Articles_node_controller {
    private final Articles_node_service articlesNodeService;
    
    public Articles_node_controller(Articles_node_service articlesNodeService) {
        this.articlesNodeService = articlesNodeService;
    }

    @GetMapping("admin/articleNode/articles")
    @Operation(summary = "Get all articles with pagination", tags={"Admin:Article"})
    public Page<ArticlesNodeDTO> getAllArticles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return articlesNodeService.getAllArticles(pageable);
    }

    @GetMapping("admin/articleNode/{articleId}")
    @Operation(summary = "Get article by ID", tags={"Admin:Article"})
    public ArticlesNodeDTO getArticle(@PathVariable String articleId) {
        return articlesNodeService.getArticle(articleId);
    }
    
    @PostMapping("admin/map/wrote_relationships")
    @Operation(summary = "Map all wrote relationships", tags={"Admin:Map"})
    public String mapAllWroteRelationships() {
        return articlesNodeService.MappAllWroteRelationships();
    }

    @PostMapping("admin/map/articles")
    @Operation(summary = "Map all articles", tags={"Admin:Map"})
    public String mapAllArticles() {
        return articlesNodeService.MappAllArticles();
    }
    
    @GetMapping("/article/{_id}/check_like")
    @Operation(summary = "Check if alredy the user liked the article", tags={"Article"})
    public ResponseEntity<Boolean> checkLike(@PathVariable String _id,Authentication auth) {
        return ResponseEntity.ok(articlesNodeService.checkLike(_id,auth.getName()));
    }

     @GetMapping("article/{_id}/count_like")
    @Operation(summary = "Counts number of likes ", tags={"Article"})
    public ResponseEntity<Integer> countLike(@PathVariable String _id) {
        return ResponseEntity.ok(articlesNodeService.countLike(_id));
    }
    
}