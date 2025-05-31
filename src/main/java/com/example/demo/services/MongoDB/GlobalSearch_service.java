package com.example.demo.services.MongoDB;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ListIterator;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.example.demo.DTO.globalSearchResult;
import com.example.demo.aggregations.DTO.facetCountDTO;
import com.example.demo.aggregations.DTO.facetResultDTO;


@Service
public class GlobalSearch_service {

    @Autowired
    private MongoTemplate mongoTemplate;
    private static final Map<String, String> collectionNameMappings;
    private static final Map<String, String> collectionAttributeMappings;
    
    static {
        collectionAttributeMappings = new HashMap<>(); // Initialize the HashMap
        collectionAttributeMappings.put("user","username");
        collectionAttributeMappings.put("team","team_name");
        collectionAttributeMappings.put("coach", "long_name");
        collectionAttributeMappings.put("player","long_name");
        
        collectionNameMappings = new HashMap<>(); // Initialize the HashMap
        collectionNameMappings.put("user","Users");
        collectionNameMappings.put("team","Teams");
        collectionNameMappings.put("coach", "Coaches");
        collectionNameMappings.put("player","Players");
    }

    // Version 1
   /* public Page<globalSearchResult> globalSearch(String keyword, Pageable pageable) {
        
       Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        Criteria commonCriteria = new Criteria().orOperator(
                Criteria.where("username").regex(pattern),     // users
                Criteria.where("long_name").regex(pattern),    // players/coaches
                Criteria.where("team_name").regex(pattern)     // teams
        );

        // Main pipeline: start with users collection
        List<Document> pipeline = new ArrayList<>();

        // Match + Project from users
        pipeline.add(new Document("$match", new Document("username",pattern)));
        pipeline.add(new Document("$project", new Document("mongo_id", "$_id")
                .append("name", "$username")
                .append("type", "user")));

        // $unionWith teams
        pipeline.add(new Document("$unionWith", new Document("coll", "Teams")
                .append("pipeline", List.of(
                        new Document("$match", new Document("team_name",pattern)),
                        new Document("$project", new Document("mongo_id", "$_id")
                                .append("name", "$team_name")
                                .append("type", "team"))
                ))
        ));

        // $unionWith coaches
        pipeline.add(new Document("$unionWith", new Document("coll", "Coaches")
                .append("pipeline", List.of(
                        new Document("$match", new Document("long_name",pattern)),
                        new Document("$project", new Document("mongo_id", "$_id")
                                .append("name", "$long_name")
                                .append("type", "coach"))
                ))
        ));

        // $unionWith players
        pipeline.add(new Document("$unionWith", new Document("coll", "Players")
                .append("pipeline", List.of(
                        new Document("$match", new Document("long_name", pattern)),
                        new Document("$project", new Document("mongo_id", "$_id")
                                .append("name", "$long_name")
                                .append("type", "player"))
                ))
        ));

        // $facet for pagination and count
        Document facetStage = new Document("$facet", new Document()
                .append("data", List.of(
                        new Document("$skip", pageable.getOffset()),
                        new Document("$limit", pageable.getPageSize())
                ))
                .append("count", List.of(
                        new Document("$count", "count")
                ))
        );
        pipeline.add(facetStage);

        // Run aggregation
        Aggregation aggregation = Aggregation.newAggregation(pipeline.stream()
                .map(stage -> (AggregationOperation) context -> stage)
                .toList());
                
        AggregationResults<facetResultDTO> results = mongoTemplate.aggregate(aggregation, "Users", facetResultDTO.class);
        facetResultDTO facetResult = results.getUniqueMappedResult();

        List<globalSearchResult> content = facetResult != null && facetResult.getData() != null
                ? facetResult.getData()
                : Collections.emptyList();

        long total = (facetResult != null && facetResult.getCount() != null && !facetResult.getCount().isEmpty())
                ? facetResult.getCount().get(0).getCount()
                : 0L;

        return new PageImpl<>(content, pageable, total);
    }*/
    // Version 2
    public Page<globalSearchResult> globalSearch(String keyword, Pageable pageable, List<String> searchInCollections) {

        Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        
        if(searchInCollections.isEmpty())
            searchInCollections.addAll(Arrays.asList("user", "coach", "team", "player"));
        

        List<Document> pipeline = new ArrayList<>();
        ListIterator<String> iterator = searchInCollections.listIterator();
        
        String collection=iterator.next();
        
        
        pipeline.add(new Document("$match", new Document(collectionAttributeMappings.get(collection), pattern)));
        pipeline.add(new Document("$project", new Document("mongo_id", "$_id")
                .append("name", "$"+collectionAttributeMappings.get(collection))
                .append("type", collection)));

        // Conditional $unionWith stages
        while(iterator.hasNext()){
            collection=iterator.next();
            pipeline.add(new Document("$unionWith", new Document("coll", collectionNameMappings.get(collection))
                    .append("pipeline", List.of(
                            new Document("$match", new Document(collectionAttributeMappings.get(collection), pattern)),
                            new Document("$project", new Document("mongo_id", "$_id")
                                    .append("name", "$"+collectionAttributeMappings.get(collection))
                                    .append("type", collection))
                    ))
            ));
        }
        
        if (pipeline.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0L);
        }
        

        // $facet for pagination and count
        Document facetStage = new Document("$facet", new Document()
                .append("data", List.of(
                        new Document("$skip", pageable.getOffset()),
                        new Document("$limit", pageable.getPageSize())
                ))
                .append("count", List.of(
                        new Document("$count", "count")
                ))
        );
        pipeline.add(facetStage);

        // Run aggregation
        Aggregation aggregation = Aggregation.newAggregation(pipeline.stream()
                .map(stage -> (AggregationOperation) context -> stage)
                .toList());
        
        
        AggregationResults<facetResultDTO> results = mongoTemplate.aggregate(aggregation,collectionNameMappings.get(collection), facetResultDTO.class);
        facetResultDTO facetResult = results.getUniqueMappedResult();

        List<globalSearchResult> content = facetResult != null && facetResult.getData() != null
                ? facetResult.getData()
                : Collections.emptyList();

        long total = (facetResult != null && facetResult.getCount() != null && !facetResult.getCount().isEmpty())
                ? facetResult.getCount().get(0).getCount()
                : 0L;

        return new PageImpl<>(content, pageable, total);
    }

}