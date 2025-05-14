package com.example.demo.projections;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


public class globalSearchResult {
    
    @Field("labels")
    private List<String> labels;
    
    @Field("properties")
    private Map<String,Object> properties;
    
}