package com.example.demo.configurations.Swagger;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

// This class will contain all your global tag definitions
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

//nested tags
@Tag(name = "Admin:Map", description="Mapping operations")
@Tag(name = "Admin:Populate", description="Database populating operations")
@Tag(name = "Admin:Player", description="Operations of admin on players")
@Tag(name = "Admin:Coach", description="Operations of admin on coaches")
@Tag(name = "Admin:Team", description="Operations of admin on teams")
@Tag(name = "Admin:User", description="Operations of admin on users")
@Tag(name = "Admin:Article", description="Operations of admin on articles")
@Tag(name = "Admin:Aggregation", description="Aggregation available for an admin")
@Tag(name = "User:Article", description="Operations done by users on their articles")


public class TagsConfiguration {
    //Leave empty
}