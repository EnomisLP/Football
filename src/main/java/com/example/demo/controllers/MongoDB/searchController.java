package com.example.demo.controllers.MongoDB;

import com.example.demo.services.MongoDB.globalSearchService;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.projections.globalSearchResult;




@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Global search", description = "QUERY FOR SEARCHING DATA")


public class searchController {
    
    private final globalSearchService gss;
    
    public searchController(globalSearchService gss){
        this.gss=gss;
    }
    
    @GetMapping("/{name}")
    @Operation(summary = "Global search")
    public ResponseEntity<Page<globalSearchResult>> search(@PathVariable String name,Pageable pageable) {
        return ResponseEntity.ok(gss.globalSearch(name, pageable));
    }
    
}