package com.example.demo.controllers.Neo4j;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.services.Neo4j.Articles_node_service;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/")


public class Articles_node_controller {
    private final Articles_node_service articlesNodeService;
    
    public Articles_node_controller(Articles_node_service articlesNodeService) {
        this.articlesNodeService = articlesNodeService;
    }

    @GetMapping("admin/articleNode/articles")
    @Operation(summary = "Get all articles with pagination", tags={"Admin"})
    public Page<ArticlesNode> getAllArticles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return articlesNodeService.getAllArticles(pageable);
    }

    @GetMapping("admin/articleNode/{articleId}")
    @Operation(summary = "Get article by ID", tags={"Admin"})
    public ArticlesNode getArticle(@PathVariable Long articleId) {
        return articlesNodeService.getArticle(articleId);
    }
    
    @PostMapping("admin/map/wrote_relationships")
    @Operation(summary = "Map all wrote relationships", tags={"Admin","Map"})
    public String mapAllWroteRelationships() {
        return articlesNodeService.MappAllWroteRelationships();
    }

    @PostMapping("admin/map/articles")
    @Operation(summary = "Map all articles", tags={"Admin","Map"})
    public String mapAllArticles() {
        return articlesNodeService.MappAllArticles();
    }
    
}