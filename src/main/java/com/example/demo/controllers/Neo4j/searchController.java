package com.example.demo.controllers.Neo4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.projections.globalSearchResult;
import com.example.demo.services.Neo4j.globalSearchService;




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
    public Page<globalSearchResult> search(@PathVariable String name,@RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return this.gss.globalSearch(name,pageable);
    }
    
}