package com.example.demo.controllers.MongoDB;

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
    
    @GetMapping("/{name}")
    @Operation(summary = "Global search")
    public ResponseEntity<Page<globalSearchResult>> search(@PathVariable String name,Pageable pageable) {
        return ResponseEntity.ok(gss.globalSearch(name, pageable));
    }
    
}