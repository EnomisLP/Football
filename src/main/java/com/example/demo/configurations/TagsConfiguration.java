package com.example.demo.configurations;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;


@Configuration
@Tag(name = "User", description = "QUERIES AND AGGREGATIONS RELATED TO USER ENTITY")
@Tag(name = "Article", description = "QUERIES AND AGGREGATIONS RELATED TO ARTICLE ENTITY")
@Tag(name = "Team", description = "QUERIES AND AGGREGATIONS RELATED TO TEAM ENTITY")
@Tag(name = "Player", description = "QUERIES AND AGGREGATIONS RELATED TO PLAYER ENTITY")
@Tag(name = "Coach", description = "QUERIES AND AGGREGATIONS RELATED TO COACH ENTITY")
@Tag(name = "Admin", description = "QUERIES AND AGGREGATIONS WHICH CAN BE EXECUTED ONLY BY AN ADMIN")
@Tag(name = "Anonymous", description = "QUERIES AND AGGREGATIONS WHICH CAN BE EXECUTED ONLY BY AN ANONYMOUS ACTOR")
@Tag(name = "Map", description = "MAPPING OPERATIONS")
@Tag(name = "Populate", description = "POPULATING OPERATIONS")
@Tag(name = "Clarify", description = "To clarify")
@Tag(name = "Aggregation", description="Aggregation")


public class TagsConfiguration {
   
}