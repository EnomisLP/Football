package com.example.demo.controllers.MongoDB;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.example.demo.DTO.globalSearchResult;
import com.example.demo.services.MongoDB.GlobalSearch_service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;




@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Global search", description = "QUERY FOR SEARCHING DATA")


public class searchController {
    
    private final GlobalSearch_service gss;
    
    public searchController(GlobalSearch_service gss){
        this.gss=gss;
    }
    
    @GetMapping("/{name}","/{name}/{filter}")
    @Operation(summary = "Global search")
    public ResponseEntity<Page<globalSearchResult>> search(@PathVariable String name,Pageable pageable,
            @PathVariable(required = false) String filter) {
        List<String> allowedFilters = Arrays.asList("user", "coach", "team", "player");
        
        
        List<String> collectionsToSearch = null;
        if (Filter == null){
            collectionsToSearch= Arrays.asList("user", "coach", "team", "player");
        }
        if (Filter != null && !Filter.trim().isEmpty()) {
            collectionsToSearch = Arrays.stream(Filter.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toList());
            
            collectionsToSearch.removeIf(s -> !allowedFilters.contains(s));
        }
        
        
        return ResponseEntity.ok(gss.globalSearch(name, pageable,collectionsToSearch));
    }
    
}